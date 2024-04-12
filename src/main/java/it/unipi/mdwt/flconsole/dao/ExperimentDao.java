package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.Experiment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentDao extends MongoRepository<Experiment, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.
}

