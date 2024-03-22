package it.unipi.mdwt.flconsole.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDAO;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.ExperimentSummary;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.ErlangMessageHandler;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.Pair;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.ls.LSException;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


import static it.unipi.mdwt.flconsole.utils.Constants.PAGE_SIZE;
import static java.lang.Thread.sleep;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ExperimentService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ExperimentDao experimentDao;
    private final ErlangMessageHandler erlangMessageHandler;
    private final Logger applicationLogger;
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;

    private final MongoTemplate mongoTemplate;


    @Autowired
    public ExperimentService(SimpMessagingTemplate messagingTemplate, ExperimentDao experimentDao, ErlangMessageHandler erlangMessageHandler, Logger applicationLogger, UserDAO userDAO, MongoTemplate mongoTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.experimentDao = experimentDao;
        this.erlangMessageHandler = erlangMessageHandler;
        this.applicationLogger = applicationLogger;
        this.userDAO = userDAO;
        this.mongoTemplate = mongoTemplate;
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
        erlangMessageHandler.initialize("provaEmail");
        erlangMessageHandler.startExperiment("exp_config");
    }




    public Experiment getExpDetails(String id) throws BusinessException {
        try {
            Optional<Experiment> expOptional = experimentDao.findById(id);
            if (expOptional.isPresent()) {
                return expOptional.get();
            } else {
                throw new BusinessException(BusinessTypeErrorsEnum.NOT_FOUND);
            }
        } catch (Exception e) {
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }


    public Page<ExperimentSummary> searchMyExperiments(String email, String expName, String configName, int page) throws BusinessException {
        try {
            if (page < 0 || PAGE_SIZE <= 0) {
                throw new IllegalArgumentException("Page and nElem must be non-negative integers.");
            }

            List<Criteria> criteriaList = new ArrayList<>();

            User user = userDAO.findByEmail(email);
            if (!StringUtils.hasText(expName) && !StringUtils.hasText(configName)) {
                return PageableExecutionUtils.getPage(user.getExperiments(), PageRequest.of(page, PAGE_SIZE), user.getExperiments()::size);
            }

            // Filter experiments based on expName and configName criteria
            List<ExperimentSummary> filteredExperiments = user.getExperiments().stream()
                    .filter(experiment -> (expName == null || experiment.getName().toLowerCase().contains(expName.toLowerCase())) &&
                            (configName == null || experiment.getConfigName().toLowerCase().contains(configName.toLowerCase())))
                    .collect(Collectors.toList());

            // Limit the result to the first 10 matching experiments
            int startIndex = page * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, filteredExperiments.size());
            List<ExperimentSummary> firstTenFilteredExperiments = filteredExperiments.subList(startIndex, endIndex);

            // Return the first 10 matching experiments as a Page object
            return new PageImpl<>(firstTenFilteredExperiments, PageRequest.of(page, PAGE_SIZE), filteredExperiments.size());


        } catch (Exception e) {
            applicationLogger.severe("Error searching experiments: " + e.getMessage());
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }






        public void saveExperiment(Experiment exp, String email) {
        experimentDao.save(exp);
        ExperimentSummary expSummary = new ExperimentSummary();
        expSummary.setId(exp.getId());
        expSummary.setName(exp.getName());
        expSummary.setCreationDate(exp.getCreationDate());
        expSummary.setConfigName(exp.getExpConfig().getName());

        Query query = new Query(where("email").is(email));
        Update update = new Update().addToSet("experiments", expSummary);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    public List<Pair<ExperimentSummary, String>> getExperimentsSummaryList(int n) {
        Pageable pageable = PageRequest.of(0, n); // First n experiments
        List<User> users = userDAO.findAll(pageable).getContent();
        List<Pair<ExperimentSummary, String>> experimentsWithAuthors = new ArrayList<>();

        for (User user : users) {
            List<ExperimentSummary> userExperiments = user.getExperiments();
            if (userExperiments != null) { // Check if the collection is not null
                for (ExperimentSummary experiment : userExperiments) {
                    String authorEmail = user.getEmail();
                    Pair<ExperimentSummary, String> experimentWithAuthor = Pair.of(experiment, authorEmail);
                    experimentsWithAuthors.add(experimentWithAuthor);
                    if (experimentsWithAuthors.size() >= n) {
                        break; // Stop iterating if we have collected enough experiments
                    }
                }
            }
        }

        return experimentsWithAuthors;
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
