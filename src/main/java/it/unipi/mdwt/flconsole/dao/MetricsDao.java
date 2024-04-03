package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricsDao extends MongoRepository<ExpMetrics, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    List<ExpMetrics> findByExpId(String expId);
}
