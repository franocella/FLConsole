package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpProgress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ExpProgressDao extends MongoRepository<ExpProgress, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    List<ExpProgress> findTopNByIdIn(List<String> configurationIds, Pageable pageable);
    ExpProgress findByName(String name);
    void deleteByName(String name);
    Boolean existsByName(String Name);
    ExpProgress findByCreationDate(Date creationDate);
    ExpProgress findByExpConfigName(String name);
    List<ExpProgress> findByExpId(String expId);
}
