package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExpConfigDao;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDAO;
import it.unipi.mdwt.flconsole.model.Experiment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExperimentServiceTest {
    private final ExperimentDao experimentDao;
    private final ExpConfigDao expConfigDao;

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    ExperimentServiceTest(ExperimentDao experimentDao,ExpConfigDao expConfigDao){
        this.experimentDao = experimentDao;
        this.expConfigDao = expConfigDao;
    }

    @Test
    void searchExpByExecutionName(){
        String expName = "experiment";

        List<Experiment> matchingExps = experimentService.searchExpByExecutionName(expName);

        assertNotNull(matchingExps);

        assertFalse((matchingExps.isEmpty()));
        for (Experiment experiment : matchingExps){
            System.out.println("Exp ID:" + experiment.getId());
            System.out.println("Exp Name: "+experiment.getName());
        }
    }



}
