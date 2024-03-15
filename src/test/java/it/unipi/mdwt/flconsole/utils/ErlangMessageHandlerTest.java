package it.unipi.mdwt.flconsole.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ErlangMessageHandlerTest {

    @Autowired
    private ErlangMessageHandler erlangMessageHandler;

    @Test
    public void testSendContentMessage() {
        // Call the sendMessage method with test data
        // When you run it, it sends a message.
    }

    @Test
    public void testSendStopMessage() {
        // Call the sendMessage method with test data
        // When you run it, it sends a message.
    }

    @Test
    public void testSendErrorMessage() {
        // Call the sendMessage method with test data
        // When you run it, it sends a message.
    }

    @Test
    public void testReceiveMessage() {
        // Call the receiveMessage method with test data
        // When you run it, it waits for a stop message.
        // It might involve running a loop like 'while(true)' until a stop message is received.
    }
}
