package it.unipi.mdwt.flconsole.utils;

import com.ericsson.otp.erlang.*;
import it.unipi.mdwt.flconsole.config.ApplicationLogConfig;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageException;
import it.unipi.mdwt.flconsole.utils.exceptions.messages.MessageTypeErrorsEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

@SpringBootTest
public class TestMailbox {

    private static final String COLLECTOR_MAILBOX = "collectorMbox";
    private OtpNode webConsoleNode;

    private static final String DIRECTOR_NODE_NAME = "directorNode";
    private static final String DIRECTOR_MAILBOX = "expRequestHandler";
    private static final String COLLECTOR_NAME_NODE = "collectorNode";

    // Get the instance of the otpNode that send requests of new experiments to the director
    // If the node does not exist, create a new one
    private OtpNode getWebConsoleNode() throws IOException {
        if (webConsoleNode == null) {
            String email = "email";
            String cookie = "cookie";
            webConsoleNode = new OtpNode(email, cookie);
        }

        return webConsoleNode;
    }

    @Test
    void serviceSimulator() {
        String config = "config";

        try {
            // Create a mailbox to send a request to the director and return the mailbox to receive the messages from the experiment node
            Pair<OtpNode, OtpMbox> expNodeInfo = sendRequest(config);

            // Get the pid of the collector if the message is an ack
            OtpErlangPid collectorPid = ackMessage(expNodeInfo.getSecond());

            // Start a new thread runnable to receive the messages from the experiment node without blocking the main thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                executor.execute(() -> receiveMessage(expNodeInfo, collectorPid));
            } finally {
                executor.shutdown(); // Shutdown the executor when done
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<OtpNode, OtpMbox> sendRequest(String config) throws IOException {

        // Get the instance of the webConsoleNode
        OtpNode webConsoleNode = getWebConsoleNode();

        // Create a mailbox to send a request to the director
        OtpMbox mboxSender = webConsoleNode.createMbox("mboxSender");

        // Create the experiment node to handle the incoming messages
        OtpNode experimentNode = new OtpNode("experimentNode", "cookie");

        // Create a mailbox to receive the request from the webConsole
        OtpMbox mboxReceiver = experimentNode.createMbox("mboxReceiver");

        // Create the message
        OtpErlangTuple message = createRequestMessage(mboxReceiver.self(), config);

        mboxSender.send(DIRECTOR_MAILBOX, DIRECTOR_NODE_NAME, message);

        mboxSender.close();

        return Pair.of(experimentNode, mboxReceiver);
    }

    private OtpErlangTuple createRequestMessage(OtpErlangPid receiver, String config) {
        OtpErlangObject[] message = new OtpErlangObject[2];
        message[0] = receiver; // PID of the receiver
        message[1] = new OtpErlangString(config); // Json serialized configuration
        return new OtpErlangTuple(message);
    }

    private OtpErlangPid ackMessage(OtpMbox mboxReceiver) {
        try {
            System.out.println("Waiting for ack message...");
            OtpErlangObject message = mboxReceiver.receive();
            if (message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                    tuple.elementAt(0) instanceof OtpErlangAtom atom && tuple.elementAt(1) instanceof OtpErlangPid pid) {
                if (!(atom.atomValue().equals("ack"))) {
                    System.out.println("Invalid ack message.");
                    throw new MessageException(MessageTypeErrorsEnum.INVALID_ACK);
                }
                System.out.println("Received ack message.");
                return pid;
            } else {
                System.out.println("Invalid message format.");
                throw new MessageException(MessageTypeErrorsEnum.INVALID_MESSAGE_FORMAT);
            }
        } catch (OtpErlangExit e) {
            System.out.println("The experiment node has been closed.");
        } catch (OtpErlangDecodeException e) {
            System.out.println("Error decoding the message.");
        }
        return null;
    }

    private void receiveMessage(Pair<OtpNode, OtpMbox> expNodeInfo, OtpErlangPid collectorPid) {
        while (true) {
            try {
                OtpErlangObject message = expNodeInfo.getSecond().receive();
                if (message instanceof OtpErlangTuple tuple && tuple.arity() == 3 &&
                        tuple.elementAt(0) instanceof OtpErlangPid senderPid && tuple.elementAt(1) instanceof OtpErlangAtom type &&
                        tuple.elementAt(2) instanceof OtpErlangString response) {
                    if (senderPid != collectorPid) {
                        continue;
                    }
                    if (type.atomValue().equals("stop")) {
                        expNodeInfo.getSecond().close();
                        expNodeInfo.getFirst().close();
                        break;
                    } else if (type.atomValue().equals("error")) {
                        // TODO: handle the error
                        System.out.println(response.stringValue());
                    } else if (type.atomValue().equals("progress")) {
                        // TODO: send the progress to the webConsole with the web socket
                        // TODO: save the progress in the database
                        System.out.println(response.stringValue());
                    } else {
                        // TODO: handle log message for type mismatch
                        System.out.println(response.stringValue());
                    }
                }
            } catch (OtpErlangExit e) {
                System.out.println("The experiment node has been closed.");
                break;
            } catch (OtpErlangDecodeException e) {
                System.out.println("Error decoding the message.");
                break;
            }
        }
        expNodeInfo.getSecond().close();
        expNodeInfo.getFirst().close();
    }

    @Test
    void directorSimulator() {
        try {
            OtpNode node = new OtpNode(DIRECTOR_NODE_NAME, "cookie");
            OtpMbox mbox = node.createMbox(DIRECTOR_MAILBOX);
            while(true) {
                System.out.println("Waiting for message...");
                OtpErlangObject message = mbox.receive();
                if (message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                        tuple.elementAt(0) instanceof OtpErlangPid pid && tuple.elementAt(1) instanceof OtpErlangString expConfig) {
                    OtpNode collectorNode = new OtpNode("collectorNode", "cookie");
                    OtpMbox collectorMbox = collectorNode.createMbox("collectorMbox");
                    OtpErlangObject[] ackMessage = new OtpErlangObject[2];
                    ackMessage[0] = new OtpErlangAtom("ack");
                    ackMessage[1] = collectorMbox.self();
                    System.out.println("Sending ack message...");
                    mbox.send(pid, new OtpErlangTuple(ackMessage));

                    System.out.println("Sending to FL partecipants...");
                    sleep(3000);
                    System.out.println("Sending to collector node...");
                    OtpErlangObject[] colMessage = new OtpErlangObject[1];
                    Random random = new Random();
                    String response =
                            "{"
                                    + "\"type\": \"data\","
                                    + "\"parameters\": {"
                                    + "\"param1\": " + random.nextInt(100) + ","
                                    + "\"param2\": " + random.nextInt(100)
                                    + "},"
                                    + "\"timestamp\": \"2024-03-13T12:34:56\","
                                    + "\"status\": \"running\""
                                    + "}";
                    colMessage[0] = new OtpErlangString(response);
                    mbox.send(collectorMbox.self(), new OtpErlangTuple(colMessage));
                } else {
                    System.out.println("Invalid message format.");
                }
            }
        } catch (IOException | OtpErlangDecodeException | OtpErlangExit | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void collectorSimulator() {
        try {
            OtpNode node = new OtpNode(COLLECTOR_NAME_NODE, "cookie");
            OtpMbox mbox = node.createMbox(COLLECTOR_MAILBOX);

            while(true) {
                System.out.println("Waiting for progresses...");
                OtpErlangObject message = mbox.receive();
                if (message instanceof OtpErlangTuple tuple && tuple.arity() == 1 &&
                        tuple.elementAt(0) instanceof OtpErlangString response) {
                    System.out.println("Received message: " + response.stringValue());
                } else {
                    System.out.println("Invalid message format.");
                }
            }

        } catch (IOException | OtpErlangDecodeException | OtpErlangExit e) {
            throw new RuntimeException(e);
        }
    }

}
