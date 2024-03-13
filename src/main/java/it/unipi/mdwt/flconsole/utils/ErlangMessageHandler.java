package it.unipi.mdwt.flconsole.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ErlangMessageHandler {

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

}
