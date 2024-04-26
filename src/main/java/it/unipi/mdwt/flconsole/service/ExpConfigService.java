package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExpConfigDao;
import it.unipi.mdwt.flconsole.dao.UserDao;
import it.unipi.mdwt.flconsole.model.ExpConfig;
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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static it.unipi.mdwt.flconsole.utils.Constants.PAGE_SIZE;

@Service
public class ExpConfigService {

    private final ExpConfigDao expConfigDao;
    private final UserDao userDao;
    private final MongoTemplate mongoTemplate;
    private final Logger applicationLogger;


    @Autowired
    public ExpConfigService(ExpConfigDao experimentDao, UserDao userDao, MongoTemplate mongoTemplate, Logger applicationLogger) {
        this.expConfigDao = experimentDao;
        this.userDao = userDao;
        this.mongoTemplate = mongoTemplate;
        this.applicationLogger = applicationLogger;
    }

    public void saveConfig(ExpConfig config, String userEmail) {
        expConfigDao.save(config);

        // Add the configuration to the user's list of configurations
        if (config.getId() != null) {
            Query query = new Query(Criteria.where("email").is(userEmail));
            Update update = new Update().addToSet("configurations", config.getId());
            mongoTemplate.updateFirst(query, update, User.class);
        }
    }

    public void deleteExpConfig(String configId, String userEmail) {
        // Delete the configuration
        expConfigDao.deleteById(configId);

        // Remove the configuration from the user's list of configurations
        Query query = new Query(Criteria.where("email").is(userEmail));
        Update update = new Update().pull("configurations", configId);
        mongoTemplate.updateFirst(query, update, User.class);

        // Mark experiments associated with the deleted configuration as deleted
        Query query2 = new Query(Criteria.where("expConfig.id").is(configId));
        Update update2 = new Update().set("expConfig.deleted", true);
        mongoTemplate.updateMulti(query2, update2, Experiment.class);
    }

    public Page<ExpConfig> getConfigsListFirstPage(List<String> configurations, Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            pageSize = PAGE_SIZE;
        }

        List<ExpConfig> configs = expConfigDao.findTopNByIdInOrderByCreationDateDesc(configurations, PageRequest.of(0, pageSize));
        return new PageImpl<>(configs, PageRequest.of(0, pageSize), configurations.size());
    }

    /**
     * Searches ExpConfig by multiple criteria and returns a page of results.
     *
     * @param configName    The name to search for.
     * @param clientStrategy   The client strategy to search for.
     * @param stopCondition The stop condition to search for.
     * @param page          The page number (0-based) to retrieve.
     * @return              A Page containing the results.
     * @throws BusinessException If an error occurs during the search.
     */
    public Page<ExpConfig> searchMyExpConfigs(String email, String configName, String clientStrategy, String stopCondition, String algorithm, int page) throws BusinessException {
        try {
            // Validate page and nElem parameters
            if (page < 0 || PAGE_SIZE <= 0) {
                throw new IllegalArgumentException("Page and nElem must be non-negative integers.");
            }

            User user = userDao.findByEmail(email);
            List<String> confList = user.getConfigurations();

            if (!StringUtils.hasText(configName) && !StringUtils.hasText(clientStrategy) && !StringUtils.hasText(stopCondition) && !StringUtils.hasText(algorithm)){
                List<ExpConfig> matchingConfigs = expConfigDao.findTopNByIdInOrderByCreationDateDesc(confList, PageRequest.of(page, PAGE_SIZE));
                return new PageImpl<>(matchingConfigs, PageRequest.of(page, PAGE_SIZE), confList.size());
            }

            // Create a list to hold the search criteria pairs
            List<Pair<String, String>> criteriaList = new ArrayList<>();

            // Add criteria pairs to the list if the values are provided and not empty
            if (configName != null && !configName.isEmpty()) {
                criteriaList.add(Pair.of("name", configName));
            }
            if (clientStrategy != null && !clientStrategy.isEmpty()) {
                criteriaList.add(Pair.of("clientSelectionStrategy", clientStrategy));
            }
            if (stopCondition != null && !stopCondition.isEmpty()) {
                criteriaList.add(Pair.of("stopCondition", stopCondition));
            }
            if (algorithm != null && !algorithm.isEmpty()) {
                criteriaList.add(Pair.of("algorithm", algorithm));
            }

            // Create a query to search for ExpConfig objects based on the provided criteria
            Query query = new Query();
            for (Pair<String, String> criterion : criteriaList) {
                query.addCriteria(Criteria.where(criterion.getFirst()).regex(criterion.getSecond(), "i"));
            }

            // Add criteria for matching the configuration IDs in the confList
            if (!confList.isEmpty()) {
                query.addCriteria(Criteria.where("id").in(confList));
            }

            // Set the page number and limit the results to the specified maximum number of elements
            query.with(PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate")));

            // Retrieve the matching ExpConfig objects from the database
            List<ExpConfig> matchingConfigs = mongoTemplate.find(query, ExpConfig.class);

            // Retrieve the total count of matching ExpConfig objects
            long totalCount = mongoTemplate.count(query, ExpConfig.class);

            // Create a Page object using the retrieved ExpConfig objects, the requested page, and the total count
            return new PageImpl<>(matchingConfigs, PageRequest.of(page, PAGE_SIZE), totalCount);
        } catch (Exception e) {
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }

    public ExpConfig getExpConfigById(String configId) {
        return expConfigDao.findById(configId).orElse(null);
    }
}

