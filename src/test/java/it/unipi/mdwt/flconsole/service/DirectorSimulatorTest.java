package it.unipi.mdwt.flconsole.service;

import com.ericsson.otp.erlang.*;
import it.unipi.mdwt.flconsole.utils.Constants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static it.unipi.mdwt.flconsole.utils.Constants.*;
import static java.lang.Thread.sleep;

//
public class DirectorSimulatorTest {

    @Test
    void directorSimulator() {
        try {
            OtpNode node = new OtpNode(DIRECTOR_NODE_NAME, COOKIE);
            OtpMbox mbox = node.createMbox(DIRECTOR_MAILBOX);

            while (true) {
                System.out.println("Director: Waiting for message...");
                OtpErlangObject message = mbox.receive();
                System.out.println("Director: Received message: " + message.toString());
                if (message instanceof OtpErlangTuple tuple && tuple.arity() == 3) {
                    if (tuple.elementAt(2) instanceof OtpErlangPid pid) {
                        OtpErlangObject[] ackMessage = new OtpErlangObject[2];
                        ackMessage[0] = new OtpErlangAtom("fl_start_str_run");
                        ackMessage[1] = new OtpErlangString("{\\\"timestamp\\\":1712206255,\\\"type\\\":\\\"experiment_queued\\\"}");
                        mbox.send(pid, new OtpErlangTuple(ackMessage));
                    }
                }
            }
        } catch (OtpErlangDecodeException | OtpErlangExit | IOException e) {
            throw new RuntimeException(e);
        }
    }

        private void collectorSimulator(OtpMbox collectorMbox) {
        try {
            Random random = new Random();
            // In this simulation after the director gives to the collector the pid
            // of the receiver the collector starts a while loop to send progress messages generated randomly.
            // In reality the collector will be in a while loop to receive the messages,
            // collect them and then send the result to the receiver
            System.out.println("Collector: Waiting for progresses...");
            OtpErlangObject message = collectorMbox.receive();
            if (message instanceof OtpErlangPid pid) {
                long startTime = System.currentTimeMillis();
                while(true) {

                    // In this simulation the collector sends a progress message every 100ms
                    // In reality the collector will send the progress messages when it receives them and collects them.
                    // After 10 seconds the collector sends different types of messages and at the end a stop message
                    // to the receiver to test the stop functionality.
                    if (System.currentTimeMillis() - startTime > 10000) {
                        OtpErlangObject[] ackMessage = new OtpErlangObject[3];
                        String response;

                        // Sends an error message to the receiver to test the error functionality
                        ackMessage[0] = collectorMbox.self();
                        ackMessage[1] = new OtpErlangAtom("error");
                        response = "There was an error during the execution of the experiment.";
                        ackMessage[2] = new OtpErlangString(response);
                        System.out.println("Collector: Sending error message to the receiver...");
                        collectorMbox.send(pid, new OtpErlangTuple(ackMessage));
                        sleep(100);

                        // Sends an invalidType message to the receiver to test the invalidType functionality
                        ackMessage[2] = new OtpErlangString(response);
                        ackMessage[1] = new OtpErlangAtom("invalidType");
                        response = "Trying sending a message of unknown type.";
                        ackMessage[2] = new OtpErlangString(response);
                        System.out.println("Collector: Sending invalidType message to the receiver...");
                        collectorMbox.send(pid, new OtpErlangTuple(ackMessage));
                        sleep(100);

                        // Sends an invalid message format message to the receiver to test the invalid message format functionality
                        System.out.println("Collector: Sending invalid message format message to the receiver...");
                        collectorMbox.send(pid, new OtpErlangAtom("Invalid message Format"));
                        sleep(100);

                        // Sends a stop message to the receiver to test the stop functionality
                        ackMessage[2] = new OtpErlangString(response);
                        ackMessage[1] = new OtpErlangAtom("stop");
                        response = "The experiment has been completed.";
                        ackMessage[2] = new OtpErlangString(response);
                        System.out.println("Collector: Sending stop message to the receiver...");
                        collectorMbox.send(pid, new OtpErlangTuple(ackMessage));
                        break;
                    }

                    OtpErlangObject[] ackMessage = new OtpErlangObject[3];
                    ackMessage[0] = collectorMbox.self();
                    System.out.println("Collector mailBox PID:" + collectorMbox.self());
                    ackMessage[1] = new OtpErlangAtom("progress");
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
                    ackMessage[2] = new OtpErlangString(response);
                    System.out.println("Collector: Sending progress message to the receiver...");
                    collectorMbox.send(pid, new OtpErlangTuple(ackMessage));
                    sleep(100);
                }
            } else if (message instanceof OtpErlangAtom atom && atom.atomValue().equals("stop")) {
                System.out.println("Collector: Stopping the collector...");
            } else {
                System.out.println("Collector: Invalid message format.");
            }

            collectorMbox.close();
            System.out.println("Collector: Collector closed.");
        } catch (OtpErlangDecodeException | OtpErlangExit | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
