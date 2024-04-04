package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.MetricsDao;
import it.unipi.mdwt.flconsole.model.ExpMetrics;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class MetricsService {

    private final MetricsDao metricsDao;
    private final MongoTemplate mongoTemplate;
    @Autowired
    public MetricsService(MetricsDao metricsDao, MongoTemplate mongoTemplate) {
        this.metricsDao = metricsDao;
        this.mongoTemplate = mongoTemplate;
    }

    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    public List<ExpMetrics> findByExpId(String expId) {
        return metricsDao.findByExpId(expId);
    }

    public void saveMetrics(ExpMetrics expMetrics) {
        metricsDao.save(expMetrics);
        switch (expMetrics.getType()) {
            case EXPERIMENT_QUEUED -> {
                Query query = new Query(where("id").is(expMetrics.getExpId()));
                Update update = new Update().set("status", "QUEUED");
                mongoTemplate.updateFirst(query, update, User.class);
            }
            case START_ROUND -> {
                if (expMetrics.getRound() == 1) {
                    Query query = new Query(where("id").is(expMetrics.getExpId()));
                    Update update = new Update().set("status", "RUNNING");
                    mongoTemplate.updateFirst(query, update, User.class);
                }
            }
        }

    }
}
