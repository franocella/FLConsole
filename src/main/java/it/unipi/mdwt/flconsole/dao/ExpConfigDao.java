package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpConfigDao extends MongoRepository<ExpConfig, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.
    @Query(fields = "{ 'role' : 1, '_id' : 1, 'name':  1, 'Algorithm':  1, 'deleted':  1, 'creationDate': 1 }")
    List<ExpConfig> findTopNByIdInOrderByCreationDateDesc(List<String> configurationIds, Pageable pageable);
}
