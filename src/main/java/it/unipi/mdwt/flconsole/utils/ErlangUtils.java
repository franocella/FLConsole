package it.unipi.mdwt.flconsole.utils;

import com.ericsson.otp.erlang.*;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageException;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static it.unipi.mdwt.flconsole.utils.Constants.*;


@Component
public class ErlangUtils {

    private OtpNode webConsoleNode;

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ErlangUtils(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    private OtpNode getWebConsoleNode(String email) throws IOException {
        if (webConsoleNode == null) {
            webConsoleNode = new OtpNode(Validator.getNameFromEmail(email), COOKIE);
        }
        return webConsoleNode;
    }

    public Pair<OtpNode, OtpMbox> sendRequest(String config, String email) throws IOException {

        // Get the instance of the webConsoleNode
        OtpNode webConsoleNode = getWebConsoleNode(email);

        // Create a mailbox to send a request to the director
        OtpMbox mboxSender = webConsoleNode.createMbox("mboxSender");

        // Create the experiment node to handle the incoming messages
        OtpNode experimentNode = new OtpNode("experimentNode", COOKIE);

        // Create a mailbox to receive the request from the webConsole
        OtpMbox mboxReceiver = experimentNode.createMbox("mboxReceiver");

        if (webConsoleNode.ping(DIRECTOR_NODE_NAME, 2000)) {
            System.out.println("Sender: Director node is up.");
        } else {
            System.out.println("Sender: Director node is down.");
            mboxSender.close();
            mboxReceiver.close();
            // TODO: handle the director node down
            throw new IOException("Director node is down.");
        }

        // Create the message
        OtpErlangTuple message = createRequestMessage(mboxReceiver.self(), config);

        System.out.println("Sender: Sending message to the director...");
        mboxSender.send(DIRECTOR_MAILBOX, DIRECTOR_NODE_NAME, message);
        mboxSender.close();
        System.out.println("Sender: Sender closed.");
        return Pair.of(experimentNode, mboxReceiver);
    }

    private OtpErlangTuple createRequestMessage(OtpErlangPid receiver, String config) {
        OtpErlangObject[] message = new OtpErlangObject[2];
        message[0] = receiver; // PID of the receiver
        message[1] = new OtpErlangString(config); // Json serialized configuration
        return new OtpErlangTuple(message);
    }

    public void ackMessage(OtpMbox mboxReceiver) {
        try {
            System.out.println("Receiver: Waiting for ack message...");
            OtpErlangObject message = mboxReceiver.receive(10000);
            if (message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                    tuple.elementAt(0) instanceof OtpErlangAtom atom && tuple.elementAt(1) instanceof OtpErlangPid pid) {
                if (atom.atomValue().equals("full")) {
                    System.out.println("Receiver: No more resources available.");
                    throw new MessageException(MessageTypeErrorsEnum.FULL_RESOURCES);

                } else if (!(atom.atomValue().equals("ack"))) {
                    System.out.println("Receiver: Invalid ack message.");
                    throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE);
                }

                System.out.println("Receiver: Received ack message.");
            } else {
                System.out.println("Receiver: Invalid ack message format.");
                throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE);
            }

        } catch (OtpErlangExit e) {
            System.out.println("Receiver: No message received.");

        } catch (OtpErlangDecodeException e) {
            System.out.println("Receiver: Error decoding the message.");
        }
    }

    public void receiveMessage(Pair<OtpNode, OtpMbox> expNodeInfo) {
        while (true) {
            try {
                System.out.println("Receiver: Waiting for message...");
                OtpErlangObject message = expNodeInfo.getSecond().receive();
                if (message instanceof OtpErlangMap map &&
                        map.get(new OtpErlangAtom("type")) != null) {
                    OtpErlangAtom type = (OtpErlangAtom) map.get(new OtpErlangAtom("type"));
                    if (type.atomValue().equals("fl_federation_end")) {
                        System.out.println("Receiver: Stopping the experiment node...");
                        expNodeInfo.getSecond().close();
                        expNodeInfo.getFirst().close();
                        break;
                    } else if (type.atomValue().equals("error")) {
                        // TODO: handle the error
                        System.out.println("Receiver: Error message received.");
                    } else if (type.atomValue().equals("strategy_server_metrics")) {
                        System.out.println("Receiver: Progress message received.");
                        // TODO: send the strategy server metrics to the webConsole with the web socket
                        try {
                            // try to send a message to the WebSocket topic
                            String jsonMessage = """
                                    {"timestamp":1711533285,"type":"strategy_server_metrics", "round": 1, "hostMetrics":\s
                                    {"cpuUsage": 15.5, "memoryUsage": 85}, "modelMetrics": {"FRO": 0.154}
                                    """;

                            messagingTemplate.convertAndSend("/experiment/progress", jsonMessage);

                            // TODO: save in the database


                        } catch (MessageDeliveryException e) {
                            System.out.println("WebSocket connection is closed. Cannot send message.");
                            break;
                        }
                        // TODO: save the strategy server metrics in the database
                    } else if (type.atomValue().equals("worker_metrics")) {
                    // TODO: send the worker metrics to the webConsole with the web socket
                    // TODO: save the worker metrics in the database
                    System.out.println("Receiver: Progress message received.");
                    } else {
                        // TODO: handle log message for type mismatch
                        System.out.println("Receiver: Unknown message type.");
                        System.out.println("Message type: " + type.atomValue());
                    }
                } else {
                    System.out.println("Receiver: Invalid message format.");
                }
            } catch (OtpErlangExit e) {
                System.out.println("Receiver: The experiment node has been closed.");
                break;
            } catch (OtpErlangDecodeException e) {
                System.out.println("Receiver: Error decoding the message.");
                break;
            }
        }
        expNodeInfo.getSecond().close();
        expNodeInfo.getFirst().close();
    }

}
