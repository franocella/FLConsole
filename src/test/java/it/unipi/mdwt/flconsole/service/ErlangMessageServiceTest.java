package it.unipi.mdwt.flconsole.service;

import com.ericsson.otp.erlang.OtpNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static it.unipi.mdwt.flconsole.utils.Constants.COOKIE;
import static it.unipi.mdwt.flconsole.utils.Constants.DIRECTOR_NODE_NAME;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ErlangMessageServiceTest {

    @Test
    void openNodeSendPingAndClose() {
        try {
            // Creating the node
            OtpNode node = new OtpNode("TestNode", COOKIE);

            // Ping to the Erlang node
            boolean pingSuccess = node.ping(DIRECTOR_NODE_NAME, 2000);

            // Verify that the ping was sent successfully
            assertTrue(pingSuccess, "Ping to the Erlang node failed.");

            sleep(4000);

            // Closing the connection
            node.close();

            // Verify that the connection is closed properly
            assertFalse(node.ping("nonexistentnode", 2000), "The connection was not closed properly.");
        } catch (Exception e) {
            fail("An exception occurred: " + e.getMessage());
        }
    }
}
