package it.unipi.mdwt.flconsole.utils;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS) // Ogni volta che viene iniettato un bean, ne viene creato uno nuovo
public class ErlangMessageHandler {

    private final String NODE_NAME="flconsole@";
    private String communicationCookie;
    private Object requestMailbox;
    private List<Object> statisticsMailboxes;
    private String directorAddress;

    public String receiveMessage() {
        Random random = new Random();

        return "{"
                + "\"type\": \"data\","
                + "\"parameters\": {"
                + "\"param1\": " + random.nextInt(100) + ","
                + "\"param2\": " + random.nextInt(100)
                + "},"
                + "\"timestamp\": \"2024-03-13T12:34:56\","
                + "\"status\": \"running\""
                + "}";
    }

/*    public String receiveMessage () {
        // Send the message to the Erlang server
        return "message";
    }*/

    public void sendMessage(String message) {
        // Send the message to the Erlang director
    }

 //fix

}
