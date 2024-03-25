package it.unipi.mdwt.flconsole.utils;

import com.ericsson.otp.erlang.*;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageException;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageTypeErrorsEnum;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static it.unipi.mdwt.flconsole.utils.Constants.*;


@Component
public class ErlangUtils {

    private OtpNode webConsoleNode;
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

    public OtpErlangPid ackMessage(OtpMbox mboxReceiver) {
        try {
            System.out.println("Receiver: Waiting for ack message...");
            OtpErlangObject message = mboxReceiver.receive();
            if (message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                    tuple.elementAt(0) instanceof OtpErlangAtom atom && tuple.elementAt(1) instanceof OtpErlangPid pid) {
                if (!(atom.atomValue().equals("ack"))) {
                    System.out.println("Receiver: Invalid ack message.");
                    throw new MessageException(MessageTypeErrorsEnum.INVALID_ACK);
                }
                System.out.println("Receiver: Received ack message.");
                return pid;
            } else {
                System.out.println("Receiver: Invalid ack message format.");
                throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE_FORMAT);
            }
        } catch (OtpErlangExit e) {
            System.out.println("The experiment node has been closed.");
        } catch (OtpErlangDecodeException e) {
            System.out.println("Error decoding the message.");
        }
        return null;
    }

    public void receiveMessage(Pair<OtpNode, OtpMbox> expNodeInfo, OtpErlangPid collectorPid) {
        while (true) {
            try {
                System.out.println("Receiver: Waiting for message...");
                OtpErlangObject message = expNodeInfo.getSecond().receive();
                if (message instanceof OtpErlangTuple tuple && tuple.arity() == 3 &&
                        tuple.elementAt(0) instanceof OtpErlangPid senderPid && tuple.elementAt(1) instanceof OtpErlangAtom type &&
                        tuple.elementAt(2) instanceof OtpErlangString response) {
                    if (!senderPid.equals(collectorPid)) {
                        System.out.println("Receiver: Invalid sender PID: " + senderPid);
                        System.out.println("Receiver: Expected PID: " + collectorPid);
                        continue;
                    }
                    if (type.atomValue().equals("stop")) {
                        System.out.println("Receiver: Stopping the experiment node...");
                        System.out.println("Message: " + response.stringValue());
                        expNodeInfo.getSecond().close();
                        expNodeInfo.getFirst().close();
                        break;
                    } else if (type.atomValue().equals("error")) {
                        // TODO: handle the error
                        System.out.println("Receiver: Error message received.");
                        System.out.println("Error message: " + response.stringValue());
                    } else if (type.atomValue().equals("progress")) {
                        // TODO: send the progress to the webConsole with the web socket
                        // TODO: save the progress in the database
                        System.out.println("Receiver: Progress message received.");
                        System.out.println("Progress message: " + response.stringValue());
                    } else {
                        // TODO: handle log message for type mismatch
                        System.out.println("Receiver: Unknown message type.");
                        System.out.println("Message type: " + type.atomValue());
                        System.out.println("Message: " + response.stringValue());
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
