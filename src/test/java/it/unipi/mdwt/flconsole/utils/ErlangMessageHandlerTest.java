package it.unipi.mdwt.flconsole.utils;

import com.ericsson.otp.erlang.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ErlangMessageHandlerTest {

    private static final String DIRECTOR_NODE_NAME = "directorNode";
    private static final String DIRECTOR_MAILBOX = "expRequestHandler";
    private static final String COOKIE = "start_exp";

    @Autowired
    private ErlangMessageHandler erlangMessageHandler;

    @BeforeEach
    void setUp() {
        erlangMessageHandler.initialize("flavio@gmail.com");
    }

    @Test
    void startExperiment() {
        erlangMessageHandler.startExperiment("expConfig");
    }

    @Test
    void DirectorSimulator() {
        // start a simulated director node that wait a message with the pid
        // of the receiver and the configuration as a string and send a static message (random data/stop message)
        try {
            OtpNode node = new OtpNode(DIRECTOR_NODE_NAME, COOKIE);
            OtpMbox mbox = node.createMbox(DIRECTOR_MAILBOX);
            while (true) {
                System.out.println("Waiting for message...");
                OtpErlangObject message = mbox.receive();
                if (message instanceof OtpErlangTuple tuple && tuple.arity() == 2 &&
                        tuple.elementAt(0) instanceof OtpErlangPid pid && tuple.elementAt(1) instanceof OtpErlangString expConfig) {
                    /*System.out.println("Received message: " + expConfig.stringValue() + " from " + pid);
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
                    OtpErlangString responseOtpString = new OtpErlangString(response);
                    mbox.send(pid, responseOtpString);
                    */
                    System.out.println("Sending stop message...");
                    String error = "{"
                            + "\"type\": \"stop\","
                            + "\"timestamp\": \"2024-03-13T12:34:56\","
                            + "\"status\": \"finished\""
                            + "}";

                    OtpErlangString responseOtpString = new OtpErlangString(error);
                    mbox.send(pid, responseOtpString);

                }

            }
        } catch (IOException | OtpErlangDecodeException | OtpErlangExit e) {
            throw new RuntimeException(e);
        }
    }
}