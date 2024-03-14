package it.unipi.mdwt.flconsole.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.utils.ErlangMessageHandler;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

@Service
public class ExperimentService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ExperimentDao experimentDao;
    private final ErlangMessageHandler erlangMessageHandler;
    private final Logger applicationLogger;

    private final ObjectMapper objectMapper;

    @Autowired
    public ExperimentService(SimpMessagingTemplate messagingTemplate, ExperimentDao experimentDao, ErlangMessageHandler erlangMessageHandler, Logger applicationLogger) {
        this.messagingTemplate = messagingTemplate;
        this.experimentDao = experimentDao;
        this.erlangMessageHandler = erlangMessageHandler;
        this.applicationLogger = applicationLogger;
        this.objectMapper = new ObjectMapper();
    }

    // Fake experiment to test WebSocket
    public void runExp() throws BusinessException{

        // Jinterface send message to the Director

        // Wait for acknowledgement from the Director

        // Start the websocket connection

        /*
        While in cui controlli se arrivano messaggi dal director, se contegono informazioni
        sulla progressione dell'esperimento li invii tramite websocket al frontend,
        se è un messaggio di errore o di stop, interrompi l'esperimento ed inoltralo
        e chiudi la connessione websocket
        */

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
                sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }


    public void runExperiment() throws BusinessException {
        // Initialization and setup code
        Map<String, Object> message;
        int i=0;
        while (i<10) {
            i++;
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String jsonMessage = erlangMessageHandler.receiveMessage();
            try {
                message = objectMapper.readValue(jsonMessage, Map.class);
            } catch (IOException e) {
                // Handle parsing exception
                continue;
            }

            assert message != null;
            if (message.get("type").equals("stop")) {
                // If it's a stop message, send the confirmation to the frontend
                    try {
                        messagingTemplate.convertAndSend("/experiment/progress", "{\"status\": \"stopped\"}");
                    } catch (MessageDeliveryException e) {
                        System.out.println("WebSocket connection is closed. Cannot send stop message.");
                    }
                break;
            } else {
                // If it's not a stop message, send only the "parameters" field to the frontend
                Map<String, String> parameters = (Map<String, String>) message.get("parameters");
                if (parameters != null) {
                    try {
                        messagingTemplate.convertAndSend("/experiment/progress", parameters);
                    } catch (MessageDeliveryException e) {
                        System.out.println("WebSocket connection is closed. Cannot send progress message.");
                        break;
                    }
                }
            }
        }
    }




    public Experiment getExpDetails(String id) throws BusinessException{
        Optional<Experiment> experiment;
        try {
            experiment = experimentDao.findById(id);
        } catch (Exception e) {
            applicationLogger.severe("An error occurred while fetching the experiment details: " + e.getMessage());
            throw new RuntimeException("An error occurred while fetching the experiment details");
        }
        return new Experiment();
    }

    public void saveExperiment(Experiment exp, String email) {
    }


/*    public Page<Experiment> getRecentExperiments(int page, int pageSize) {
        // Sorting criterion for creationDate in descending order
        Sort sort = Sort.by(Sort.Order.desc("creationDate"));

        // Creating a Pageable object
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        // Calling the repository to retrieve paginated and sorted experiments
        return experimentDao.findAllOrderByCreationDateDesc(pageable);
    }*/
}
