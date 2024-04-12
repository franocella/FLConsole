package it.unipi.mdwt.flconsole.service;

import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;
import it.unipi.mdwt.flconsole.dao.MetricsDao;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDao;
import it.unipi.mdwt.flconsole.model.ExpMetrics;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.ExperimentSummary;
import it.unipi.mdwt.flconsole.model.User;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.stream.Collectors;


import static it.unipi.mdwt.flconsole.utils.Constants.PAGE_SIZE;
import static java.lang.Thread.sleep;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ExperimentService {

    private final ExperimentDao experimentDao;
    private final Logger applicationLogger;
    private final UserDao userDAO;
    private final MessageService messageService;
    private final MongoTemplate mongoTemplate;
    private final ExecutorService experimentExecutor;
    private final MetricsDao metricsDao;

    @Autowired
    public ExperimentService(ExperimentDao experimentDao, Logger applicationLogger,
                             UserDao userDAO, MessageService messageService, MongoTemplate mongoTemplate, ExecutorService executorService, MetricsDao metricsDao) {
        this.experimentDao = experimentDao;
        this.applicationLogger = applicationLogger;
        this.userDAO = userDAO;
        this.messageService = messageService;
        this.mongoTemplate = mongoTemplate;
        this.metricsDao = metricsDao;
        this.experimentExecutor = executorService;
    }

    // View Experiment progress
    // Check the status of the experiment from the database and retrieve the metrics
    // if the experiment status is "running" subscribe to the experiment metrics broadcast



    /*

        runExp
        Utente admin manda richiesta di esecuzione di un esperimento
        Viene chimato il meccanismo di comunicazione con Erlang
        Viene ricuvuto l'ack
        Viene inviato in broadcast il messaggio con le metriche
        Viene salvato il messaggio con le metriche  nel db

        una volta ricevuto il messaggio di stop dal director viene chiuso il broadcast e vengono fatti disconnettere i clients

    */

    /**
     * This method runs an experiment based on the provided configuration and email.
     *
     * @param config The serialized JSON string with the configuration and the name for the experiment.
     * @param email The email associated with the experiment.
     * @throws BusinessException If an error occurs during the execution of the experiment.
     */
    public void runExp(String config, String email, String expId) throws BusinessException{

/*            // Create a mailbox to send a request to the director and return the mailbox to receive the messages from the experiment node
            applicationLogger.severe("Sending request to director: " + System.currentTimeMillis() );
            Pair<OtpNode, OtpMbox> expNodeInfo = messageService.sendRequest(config, email, expId);
            applicationLogger.severe("Request sent: " + System.currentTimeMillis());

            // Wait for the director to send an acknowledgment message to the experiment node
            messageService.ackMessage(expNodeInfo.getSecond(), expId);
            messageService.sendAndAck(config, expId);
            applicationLogger.severe("Received ack message: " + System.currentTimeMillis() );

            //Start a new thread runnable to receive the messages from the experiment node without blocking the main thread
            experimentExecutor.execute(() -> messageService.receiveMessage(expNodeInfo, expId));*/

            experimentExecutor.execute(() ->messageService.sendAndMonitor(config, expId));

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
