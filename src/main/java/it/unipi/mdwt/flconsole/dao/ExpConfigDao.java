package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.Experiment;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpConfigDao extends MongoRepository<ExpConfig, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    List<ExpConfig> findByIdIn(List<String> configurationIds);
    List<ExpConfig> findTopNByIdIn(List<String> configurationIds, Pageable pageable);
    ExpConfig findByName(String name);
    void deleteByName(String name);
    boolean existsByName(String name);
    List<ExpConfig> findByStrategy(String strategy);
    List<ExpConfig> findByStopCondition(String stopCondition);


}
