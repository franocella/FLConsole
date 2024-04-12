package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDao;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.dto.ExperimentSummary;
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
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static it.unipi.mdwt.flconsole.utils.Constants.PAGE_SIZE;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ExperimentService {

    private final ExperimentDao experimentDao;
    private final Logger applicationLogger;
    private final UserDao userDAO;
    private final MessageService messageService;
    private final MongoTemplate mongoTemplate;
    private final ExecutorService experimentExecutor;

    @Autowired
    public ExperimentService(ExperimentDao experimentDao, Logger applicationLogger,
                             UserDao userDAO, MessageService messageService, MongoTemplate mongoTemplate, ExecutorService executorService) {
        this.experimentDao = experimentDao;
        this.applicationLogger = applicationLogger;
        this.userDAO = userDAO;
        this.messageService = messageService;
        this.mongoTemplate = mongoTemplate;
        this.experimentExecutor = executorService;
    }

    public void runExp(String config, String expId) throws BusinessException{
            experimentExecutor.execute(() -> messageService.sendAndMonitor(config, expId));
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

            // Filter the experiments by name and configuration name and sort them by creationDate in descending order
            List<ExperimentSummary> filteredExperiments = user.getExperiments().stream()
                    .filter(experiment -> (expName == null || experiment.getName().toLowerCase().contains(expName.toLowerCase())) &&
                            (configName == null || experiment.getConfigName().toLowerCase().contains(configName.toLowerCase())))
                    .sorted(Comparator.comparing(ExperimentSummary::getCreationDate).reversed()).collect(Collectors.toList());

            // Calculate the start and end index for pagination
            int startIndex = page * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, filteredExperiments.size());

            // Extract the sublist for the current page
            List<ExperimentSummary> pagedExperiments = filteredExperiments.subList(startIndex, endIndex);

            // Return the sorted and paginated experiments as a Page object
            return new PageImpl<>(pagedExperiments, PageRequest.of(page, PAGE_SIZE), filteredExperiments.size());

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

    public void deleteExperiment(String expId, String email) {
        // Delete the experiment
        experimentDao.deleteById(expId);

        // Remove the experiment from the user's list of experiments
        Query query = new Query(where("email").is(email));
        Update update = new Update().pull("experiments", new Query(where("id").is(expId)));
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
}
