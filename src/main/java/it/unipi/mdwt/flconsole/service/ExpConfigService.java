package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExpConfigDao;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDAO;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class ExpConfigService {

    private final ExpConfigDao experimentDao;
    private final UserDAO userDAO;
    private final MongoTemplate mongoTemplate;

    private final Logger applicationLogger;


    @Autowired
    public ExpConfigService(ExpConfigDao experimentDao, UserDAO userDAO, MongoTemplate mongoTemplate, Logger applicationLogger) {
        this.experimentDao = experimentDao;
        this.userDAO = userDAO;
        this.mongoTemplate = mongoTemplate;
        this.applicationLogger = applicationLogger;
    }

    public List<ExpConfig> getUsersConfigList() {
        return null;
    }

    public void saveConfig(ExpConfig config, String userEmail) {
        experimentDao.save(config);

        // Add the configuration to the user's list of configurations
        if (config.getId() != null) {
            Query query = new Query(Criteria.where("email").is(userEmail));
            Update update = new Update().addToSet("configurations", config.getId());
            mongoTemplate.updateFirst(query, update, User.class);
        }
    }

    public void deleteConfig(String configId, String userEmail) {
        // Delete the configuration
        experimentDao.deleteById(configId);

        // Remove the configuration from the user's list of configurations
        Query query = new Query(Criteria.where("email").is(userEmail));
        Update update = new Update().pull("configurations", configId);
        mongoTemplate.updateFirst(query, update, User.class);
    }

}
