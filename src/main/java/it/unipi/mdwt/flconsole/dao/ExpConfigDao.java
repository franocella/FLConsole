package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpConfigDao extends MongoRepository<ExpConfig, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    List<ExpConfig> findByIdIn(List<String> configurationIds);

    ExpConfig findByName(String name);
    void deleteByName(String name);
    boolean existsByName(String name);

}
