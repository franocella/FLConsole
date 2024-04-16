package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.config.ExecutorConfig;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDao;
import it.unipi.mdwt.flconsole.dto.ExperimentSummary;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static it.unipi.mdwt.flconsole.utils.Constants.PAGE_SIZE;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ExperimentService {

    private final ExperimentDao experimentDao;
    private final Logger applicationLogger;
    private final MessageService messageService;
    private final MongoTemplate mongoTemplate;
    private final UserDao userDao;

    @Autowired
    public ExperimentService(ExperimentDao experimentDao, Logger applicationLogger, MessageService messageService, MongoTemplate mongoTemplate, UserDao userDao) {
        this.experimentDao = experimentDao;
        this.applicationLogger = applicationLogger;
        this.messageService = messageService;
        this.mongoTemplate = mongoTemplate;
        this.userDao = userDao;
    }

    /**
     * Executes an experiment using the specified configuration and experiment ID.
     * This method invokes {@link ExecutorService#execute(Runnable)} to run
     * the experiment in a separate thread in a non-blocking manner, which in turn calls
     * {@link MessageService#sendAndMonitor(String, String)} to send and monitor the message.
     *
     * @param config The experiment configuration.
     * @param expId The experiment ID.
     * @throws BusinessException If an error occurs during the experiment execution.
     */
    public void runExp(String config, String expId) throws BusinessException {
        ExecutorService experimentExecutor = ExecutorConfig.getInstance();
        experimentExecutor.execute(() -> messageService.sendAndMonitor(config, expId));
    }

    /**
     * Retrieves details of an experiment with the specified ID.
     *
     * @param id The ID of the experiment to retrieve.
     * @return The experiment details, if found.
     * @throws BusinessException If an error occurs during the retrieval process, or if the experiment is not found.
     */
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

    /**
     * Retrieves a page of experiments belonging to a user, filtered by experiment name and configuration name.
     *
     * @param email The email of the user whose experiments are to be retrieved.
     * @param expName The name of the experiment to filter by. Can be null to skip filtering.
     * @param configName The name of the configuration to filter by. Can be null to skip filtering.
     * @param page The page number of results to retrieve.
     * @return A page of experiments matching the specified criteria.
     * @throws BusinessException If an error occurs during the retrieval process.
     */
    public Page<ExperimentSummary> getMyExperiments(String email, String expName, String configName, int page) throws BusinessException {
        try {
            if (page < 0 || PAGE_SIZE <= 0) {
                throw new IllegalArgumentException("Page and nElem must be non-negative integers.");
            }

            // Retrieve the user based on the provided email
            User user = userDao.findByEmail(email);

            // Filter the experiments by name and configuration name, and sort them by creationDate in descending order
            List<ExperimentSummary> filteredExperiments = user.getExperiments().stream()
                    .filter(experiment -> (expName == null || experiment.getName().toLowerCase().contains(expName.toLowerCase())) &&
                            (configName == null || experiment.getConfigName().toLowerCase().contains(configName.toLowerCase())))
                    .sorted(Comparator.comparing(ExperimentSummary::getCreationDate).reversed())
                    .collect(Collectors.toList());

            // Calculate the start and end index for pagination
            int startIndex = page * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, filteredExperiments.size());

            // Extract the sublist for the current page
            List<ExperimentSummary> pagedExperiments = filteredExperiments.subList(startIndex, endIndex);

            // Return the sorted and paginated experiments as a Page object
            return new PageImpl<>(pagedExperiments, PageRequest.of(page, PAGE_SIZE), filteredExperiments.size());

        } catch (Exception e) {
            // Log any errors that occur during the process
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Saves an experiment and updates the user's list of experiments.
     *
     * @param exp The experiment to save.
     * @param email The email of the user associated with the experiment.
     */
    public void saveExperiment(Experiment exp, String email) {
        // Save the experiment
        experimentDao.save(exp);

        // Create a summary of the experiment
        ExperimentSummary expSummary = new ExperimentSummary();
        expSummary.setId(exp.getId());
        expSummary.setName(exp.getName());
        expSummary.setCreationDate(exp.getCreationDate());
        expSummary.setConfigName(exp.getExpConfig().getName());

        // Create a query to find the user by email
        Query query = new Query(where("email").is(email));

        // Update the user's list of experiments
        Update update = new Update().addToSet("experiments", expSummary);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    /**
     * Deletes an experiment and related data.
     *
     * @param expId The ID of the experiment to delete.
     * @param email The email of the user associated with the experiment.
     */
    public void deleteExperiment(String expId, String email) {
        // Delete the experiment
        experimentDao.deleteById(expId);

        // Remove the experiment from the user's list of experiments
        Query userQuery = new Query(Criteria.where("email").is(email));
        Update userUpdate = new Update().pull("experiments", Query.query(Criteria.where("id").is(expId)));
        mongoTemplate.updateFirst(userQuery, userUpdate, User.class);

        // Remove the metrics linked to the deleted experiment
        Query metricsQuery = new Query(Criteria.where("expId").is(expId));
        mongoTemplate.remove(metricsQuery, "expMetrics");
    }

    /**
     * Retrieves a page of experiments filtered by experiment name and configuration name.
     *
     * @param expName The name of the experiment to filter by. Can be null or empty to skip filtering.
     * @param configName The name of the configuration to filter by. Can be null or empty to skip filtering.
     * @param page The page number of results to retrieve.
     * @return A page of experiments matching the specified criteria.
     * @throws BusinessException If an error occurs during the retrieval process.
     */
    public Page<Experiment> getExperiments(String expName, String configName, int page) {
        try {
            if (page < 0 || PAGE_SIZE <= 0) {
                throw new IllegalArgumentException("Page and nElem must be non-negative integers.");
            }

            // Create a list to hold the search criteria pairs
            List<Pair<String, String>> criteriaList = new ArrayList<>();

            // Add criteria pairs to the list if the values are provided and not empty
            if (expName != null && !expName.isEmpty()) {
                criteriaList.add(Pair.of("name", expName));
                applicationLogger.severe("ExpName: " + expName);
            }
            if (configName != null && !configName.isEmpty()) {
                criteriaList.add(Pair.of("expConfig.name", configName));
                applicationLogger.severe("ConfigName: " + configName);
            }

            // Create a query to search for experiments based on the provided criteria
            Query query = new Query();
            for (Pair<String, String> criterion : criteriaList) {
                query.addCriteria(Criteria.where(criterion.getFirst()).regex(criterion.getSecond(), "i"));
            }

            // Retrieve the total count of matching experiments
            long totalCount = mongoTemplate.count(query, Experiment.class);
            applicationLogger.severe("Total count all experiments: " + totalCount);

            // Set the page number and limit the results to the specified maximum number of elements
            query.with(PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate")));

            // Retrieve the matching experiments from the database
            List<Experiment> matchingExperiments = mongoTemplate.find(query, Experiment.class);
            applicationLogger.severe("Matching experiments: " + matchingExperiments.size());

            // Create a Page object using the retrieved experiments, the requested page, and the total count
            return new PageImpl<>(matchingExperiments, PageRequest.of(page, PAGE_SIZE), totalCount);
        } catch (Exception e) {
            // Log any errors that occur during the process
            applicationLogger.severe("Error searching experiments: " + e.getMessage());
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }
}
