package it.unipi.mdwt.flconsole.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ExpRunnerService {

    private final SimpMessagingTemplate messagingTemplate;


    @Autowired
    public ExpRunnerService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
    }

    // Fake experiment to test WebSocket
    public void runExp() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 10000;

        Random random = new Random();

        while (System.currentTimeMillis() < endTime) {
            int randomNumber = random.nextInt(100);

            try {
                // try to send a message to the WebSocket topic
                String jsonMessage = String.format("{\"%s\": %d}", "RandomValue", randomNumber);
                messagingTemplate.convertAndSend("/experiment/progress", jsonMessage);

            } catch (MessageDeliveryException e) {
                System.out.println("WebSocket connection is closed. Cannot send message.");
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
