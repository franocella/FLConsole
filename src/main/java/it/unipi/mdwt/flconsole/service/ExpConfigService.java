package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExpConfigDao;
import it.unipi.mdwt.flconsole.dao.UserDAO;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    public List<ExpConfig> searchExpConfigByConfigName(String name) throws BusinessException{
        try{
            List<ExpConfig> matchingConfigs = new ArrayList<>();

            Query query = new Query();
            query.addCriteria(Criteria.where("name").regex(name,"i"));
            List<ExpConfig> configsByTemplate = mongoTemplate.find(query, ExpConfig.class);
            matchingConfigs.addAll(configsByTemplate);

            return matchingConfigs;

        }catch (Exception e){
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }
    public List<ExpConfig> searchExpConfigByStrategy(String strategy)throws BusinessException{
        try{
            List<ExpConfig> expConfigs = expConfigDao.findByStrategy(strategy);
            if (!expConfigs.isEmpty()){
                return expConfigs;
            }else{
                throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
            }

        }catch (Exception e){
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }
    public List<ExpConfig> searchExpConfigByStopCondition(String stopCondition)throws BusinessException{
        try{
            List<ExpConfig> expConfigs = expConfigDao.findByStrategy(stopCondition);
            if (!expConfigs.isEmpty()){
                return expConfigs;
            }else{
                throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
            }

        }catch (Exception e){
            throw new BusinessException(BusinessTypeErrorsEnum.INTERNAL_SERVER_ERROR);
        }
    }






}
//name, client strategy, stop condition
