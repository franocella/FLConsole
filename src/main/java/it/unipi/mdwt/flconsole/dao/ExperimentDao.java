package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.Experiment;
import org.springframework.stereotype.Repository;

@Repository
public class ExperimentDao {
    public Experiment findById(String id) {
        return new Experiment();
    }
}
