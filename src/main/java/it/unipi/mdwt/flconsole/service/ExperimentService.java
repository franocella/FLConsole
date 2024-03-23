package it.unipi.mdwt.flconsole.service;

import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDAO;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.ExperimentSummary;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.ErlangMessageHandler;
import it.unipi.mdwt.flconsole.utils.ErlangUtils;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.Pair;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private final ErlangUtils erlangUtils;
    private final MongoTemplate mongoTemplate;


    @Autowired
    public ExperimentService(SimpMessagingTemplate messagingTemplate, ExperimentDao experimentDao, ErlangMessageHandler erlangMessageHandler, Logger applicationLogger, UserDAO userDAO, ErlangUtils erlangUtils, MongoTemplate mongoTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.experimentDao = experimentDao;
        this.erlangMessageHandler = erlangMessageHandler;
        this.applicationLogger = applicationLogger;
        this.userDAO = userDAO;
        this.erlangUtils = erlangUtils;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = new ObjectMapper();
    }



    /**
     * This method runs an experiment based on the provided configuration and email.
     *
     * @param config The serialized JSON string with the configuration and the name for the experiment.
     * @param email The email associated with the experiment.
     * @throws BusinessException If an error occurs during the execution of the experiment.
     */
    public void runExp(String config, String email) throws BusinessException{
        try {
            // Create a mailbox to send a request to the director and return the mailbox to receive the messages from the experiment node
            Pair<OtpNode, OtpMbox> expNodeInfo = erlangUtils.sendRequest(config, email);

            // Get the pid of the collector if the message is an ack
            OtpErlangPid collectorPid = erlangUtils.ackMessage(expNodeInfo.getSecond());

            // Start a new thread runnable to receive the messages from the experiment node without blocking the main thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                executor.execute(() -> erlangUtils.receiveMessage(expNodeInfo, collectorPid));
            } finally {
                executor.shutdown(); // Shutdown the executor when done
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

            User user = userDAO.findByEmail(email);
            if (!StringUtils.hasText(expName) && !StringUtils.hasText(configName)) {
                // Return the first PAGE_SIZE experiments
                List<ExperimentSummary> pagedExperiments = user.getExperiments().subList(page * PAGE_SIZE, Math.min((page + 1) * PAGE_SIZE, user.getExperiments().size()));
                return PageableExecutionUtils.getPage(pagedExperiments, PageRequest.of(page, PAGE_SIZE), user.getExperiments()::size);
            }

            // Filter experiments based on expName and configName criteria
            List<ExperimentSummary> filteredExperiments = user.getExperiments().stream()
                    .filter(experiment -> (expName == null || experiment.getName().toLowerCase().contains(expName.toLowerCase())) &&
                            (configName == null || experiment.getConfigName().toLowerCase().contains(configName.toLowerCase())))
                    .collect(Collectors.toList());

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
