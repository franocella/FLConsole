package it.unipi.mdwt.flconsole.utils;

import com.ericsson.otp.erlang.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.model.ExpMetrics;
import it.unipi.mdwt.flconsole.service.MetricsService;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageException;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import it.unipi.mdwt.flconsole.utils.MessageType;

import java.io.IOException;

import static it.unipi.mdwt.flconsole.utils.Constants.*;


@Component
public class ErlangUtils {

    private OtpNode webConsoleNode;

    private final SimpMessagingTemplate messagingTemplate;

    private final MetricsService metricsService;

    @Autowired
    public ErlangUtils(SimpMessagingTemplate messagingTemplate, MetricsService metricsService) {
        this.messagingTemplate = messagingTemplate;
        this.metricsService = metricsService;
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

    public void ackMessage(OtpMbox mboxReceiver, String expId) {
        try {
            System.out.println("Receiver: Waiting for ack message...");
            OtpErlangObject message = mboxReceiver.receive(10000);

            if (
                    message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                    tuple.elementAt(0) instanceof OtpErlangAtom atom && tuple.elementAt(1) instanceof OtpErlangString info &&
                    atom.atomValue().equals("fl_start_str_run")
            ) {
                String jsonMessage = info.stringValue();
                ObjectMapper objectMapper = new ObjectMapper();
                ExpMetrics expMetrics = objectMapper.readValue(jsonMessage, ExpMetrics.class);

                if (expMetrics.getType() == MessageType.EXPERIMENT_QUEUED) {
                    // try to send a message to the WebSocket topic
                    messagingTemplate.convertAndSend("/experiment/" + expId + "/metrics", jsonMessage);

                    expMetrics.setExpId(expId);
                    metricsService.saveMetrics(expMetrics);
                    System.out.println("Receiver: Received ack message.");
                } else {
                    System.out.println("Receiver: Invalid ack message type.");
                    throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE);
                }
            } else {
                System.out.println("Receiver: Invalid ack message format.");
                throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE);
            }

        } catch (OtpErlangExit e) {
            System.out.println("Receiver: No message received.");

        } catch (OtpErlangDecodeException e) {
            System.out.println("Receiver: Error decoding the message.");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage(Pair<OtpNode, OtpMbox> expNodeInfo, String expId) {
        while (true) {
            try {
                System.out.println("Receiver: Waiting for message...");
                OtpErlangObject message = expNodeInfo.getSecond().receive();

                if (
                        message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                                tuple.elementAt(0) instanceof OtpErlangAtom atom
                                && tuple.elementAt(1) instanceof OtpErlangString info &&
                                atom.atomValue().equals("fl_message")
                ) {
                    String jsonMessage = info.stringValue();
                    ObjectMapper objectMapper = new ObjectMapper();
                    ExpMetrics expMetrics = objectMapper.readValue(jsonMessage, ExpMetrics.class);

                    // try to send a message to the WebSocket topic
                    messagingTemplate.convertAndSend("/experiment/" + expId + "/metrics", jsonMessage);

                    expMetrics.setExpId(expId);
                    metricsService.saveMetrics(expMetrics);

                    switch (expMetrics.getType()) {
                        case STRATEGY_SERVER_READY -> {
                            System.out.println("Receiver: Strategy server ready message received.");
                        }
                        case WORKER_READY -> {
                            System.out.println("Receiver: Worker ready message received.");
                        }
                        case ALL_WORKERS_READY -> {
                            System.out.println("Receiver: All workers ready message received.");
                        }
                        case START_ROUND -> {
                            System.out.println("Receiver: Start round message received.");
                        }
                        case WORKER_METRICS -> {
                            System.out.println("Receiver: Progress message received.");
                        }
                        case STRATEGY_SERVER_METRICS -> {
                            System.out.println("Receiver: Strategy server metrics message received.");
                        }
                        case END_ROUND -> {
                            System.out.println("Receiver: End round message received.");
                        }
                        default -> {
                            System.out.println("Receiver: Invalid message type.");
                        }
                    }
                } else if (
                        message instanceof OtpErlangTuple tuple && tuple.arity() == 3 &&
                                tuple.elementAt(0) instanceof OtpErlangAtom atom
                                && atom.atomValue().equals("fl_end_str_run")
                ) {
                    // TODO: send the end experiment message to the webConsole with the web socket,
                    //  store the list of bytes in a file in the file system and update the status of the experiment
                    System.out.println("Receiver: End experiment message received.");
                    expNodeInfo.getSecond().close();
                    expNodeInfo.getFirst().close();
                    break;
                } else {
                    System.out.println("Receiver: Invalid message format.");
                }
            } catch (OtpErlangExit e) {
                System.out.println("Receiver: The experiment node has been closed.");
                break;
            } catch (OtpErlangDecodeException e) {
                System.out.println("Receiver: Error decoding the message.");
                break;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
