package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExpConfigDao;
import it.unipi.mdwt.flconsole.dao.UserDAO;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ExpConfigService {

    private final ExpConfigDao expConfigDao;
    private final UserDAO userDAO;
    private final MongoTemplate mongoTemplate;

    private final Logger applicationLogger;


    @Autowired
    public ExpConfigService(ExpConfigDao experimentDao, UserDAO userDAO, MongoTemplate mongoTemplate, Logger applicationLogger) {
        this.expConfigDao = experimentDao;
        this.userDAO = userDAO;
        this.mongoTemplate = mongoTemplate;
        this.applicationLogger = applicationLogger;
    }

    public List<ExpConfig> getExpConfigsForUser(String email) {
        User user = userDAO.findByEmail(email);

        if (user != null) {
            List<String> configurationIds = user.getConfigurations();

            if (configurationIds != null && !configurationIds.isEmpty()) {
                List<String> configurationIdStrings = new ArrayList<>(configurationIds);

                return expConfigDao.findByIdIn(configurationIdStrings);
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
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
    }


    public List<ExpConfig> getExpConfigsList(List<String> configurations) {
        return expConfigDao.findByIdIn(configurations);
    }

    public List<ExpConfig> searchExpConfigByConfigName(String name, int nElem) throws BusinessException{
        try{

            Query query = new Query();
            query.addCriteria(Criteria.where("name").regex(name,"i"));
            List<ExpConfig> configsByTemplate = mongoTemplate.find(query, ExpConfig.class);

            return new ArrayList<>(configsByTemplate);

        }catch (Exception e){
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Searches ExpConfig by multiple criteria and returns a page of results.
     *
     * @param configName    The name to search for.
     * @param clientStrategy   The client strategy to search for.
     * @param stopCondition The stop condition to search for.
     * @param page          The page number (0-based) to retrieve.
     * @param nElem         The maximum number of elements per page.
     * @return              A Page containing the results.
     * @throws BusinessException If an error occurs during the search.
     */
    public Page<ExpConfig> searchMyExpConfigs(String email, String configName, String clientStrategy, String stopCondition, int page, int nElem) throws BusinessException {
        try {
            // Validate page and nElem parameters
            if (page < 0 || nElem <= 0) {
                throw new IllegalArgumentException("Page and nElem must be non-negative integers.");
            }

            User user = userDAO.findByEmail(email);
            List<String> confList = user.getConfigurations();

            if (!StringUtils.hasText(configName) && !StringUtils.hasText(clientStrategy) && !StringUtils.hasText(stopCondition)) {
                List<ExpConfig> matchingConfigs = expConfigDao.findTopNByIdIn(confList, PageRequest.of(page, nElem));
                return PageableExecutionUtils.getPage(matchingConfigs, PageRequest.of(page, nElem), confList::size);
            }

            // Create a list to hold the search criteria pairs
            List<Pair<String, String>> criteriaList = new ArrayList<>();

            // Add criteria pairs to the list if the values are provided and not empty
            if (configName != null && !configName.isEmpty()) {
                criteriaList.add(Pair.of("name", configName));
            }
            if (clientStrategy != null && !clientStrategy.isEmpty()) {
                criteriaList.add(Pair.of("strategy", clientStrategy));
            }
            if (stopCondition != null && !stopCondition.isEmpty()) {
                criteriaList.add(Pair.of("stopCondition", stopCondition));
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
            query.with(PageRequest.of(page, nElem));
            // Retrieve the matching ExpConfig objects from the database
            List<ExpConfig> matchingConfigs = mongoTemplate.find(query, ExpConfig.class);

            // Retrieve the total count of matching ExpConfig objects
            long totalCount = mongoTemplate.count(query, ExpConfig.class);

            // Create a Page object using the retrieved ExpConfig objects, the requested page, and the total count
            return PageableExecutionUtils.getPage(matchingConfigs, PageRequest.of(page, nElem), () -> totalCount);
        } catch (Exception e) {
            // Log the exception details
            // logger.error("Error occurred while searching for ExpConfig.", e);
            // If an error occurs during the search, throw a BusinessException
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }
}

