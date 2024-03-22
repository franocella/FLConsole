package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExperimentDao extends MongoRepository<Experiment, String> {

    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    List<Experiment> findTopNByIdIn(List<String> configurationIds, Pageable pageable);
    Experiment findByName(String name);
    void deleteByName(String name);
    Boolean existsByName(String Name);
    Experiment findByCreationDate(Date creationDate);
    Experiment findByExpConfigName(String name);




}

