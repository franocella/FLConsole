package it.unipi.mdwt.flconsole.dao;


import it.unipi.mdwt.flconsole.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DataIntegrityViolationException;
import com.mongodb.DuplicateKeyException;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains JUnit tests for the ExperimentDAO class.
 */

@DataMongoTest
class ExperimentDaoTest {

    @Autowired
    private ExperimentDao experimentDao;

    @Autowired
    private  ExpConfigDao expConfigDao;

    @Autowired
    private UserDAO userDao;

    /**
     * test to save method of the ExperimentDAO
     */
    @Test
    void save(){
        //create an experiment object
        Experiment experiment = new Experiment();
        experiment.setName("Save Test Experiment4");

        Optional<ExpConfig> config = expConfigDao.findById("65f47a1bf5ad864859550aab");

        //if config exist then get the parameter create the config summary
        if (config.isPresent()){
            ExpConfig expConfig = config.get();
            ExpConfigSummary expConfigSummary = new ExpConfigSummary();
            expConfigSummary.setId(expConfig.getId());
            expConfigSummary.setName(expConfig.getName());
            expConfigSummary.setAlgorithm(expConfig.getAlgorithm());
            experiment.setExpConfigSummary(expConfigSummary);
        }
        else {
            System.out.println("ExpConfig with that id not found");
        }

        List<ExpProgress> expProgresses = new ArrayList<>();
        experiment.setProgressList(expProgresses);

        Experiment savedExperiment = experimentDao.save(experiment);

        assertNotNull(savedExperiment.getId());
        assertEquals("Save Test Experiment",savedExperiment.getName());

        System.out.println("first experiment is created");

    }
    /**
     * Test the update method of the ExperimentDAO.
     */
    @Test
    void update(){
        //create an experiment object
        Experiment experiment = new Experiment();
        experiment.setName("Save Test Experiment2");

        Experiment savedExperiment = experimentDao.save(experiment);

        savedExperiment.setName("Changed Name2");
        experimentDao.save(savedExperiment);

        Optional<Experiment> updatedExperiment = experimentDao.findById(savedExperiment.getId());
        assertTrue(updatedExperiment.isPresent());
        assertEquals("Changed Name", updatedExperiment.get().getName());
    }

    /**
     * Test the delete method of the UserDAO.
     */
    @Test
    void delete(){
       String nameToDelete = "Save Test Experiment4";
       Experiment experiment = experimentDao.findByName(nameToDelete);

       assertNotNull(experiment);

       ExpConfigSummary expConfigSummary = experiment.getExpConfigSummary();
       Optional<ExpConfig> expConfig = expConfigDao.findById(expConfigSummary.getId());

       assertNotNull(expConfigSummary);
       assertNotNull(expConfig);

        // Get the users associated with the experiment
        List<User> users = userDao.findAll();
        for (User user: users){
            List<ExperimentSummary> experiments = user.getExperiments();
            experiments.removeIf(exp -> exp.getId().equals(experiment.getId()));
            user.setExperiments(experiments);
            userDao.save(user);
        }

       experimentDao.deleteByName(nameToDelete);
       expConfigDao.deleteById(expConfigSummary.getId());

        // Check if the experiment is deleted
        assertFalse(experimentDao.existsByName(nameToDelete));

        // Check if linked ExpConfig is deleted
        assertNull(expConfigDao.findById(expConfig.get().getId()));
    }

    /**
     * Test the findByName method of the ExperimentDao.
     */
    @Test
    void findByName(){
        // Given
        Experiment experiment = new Experiment();
        experiment.setName("Test Experiment");

        // When
        Experiment foundExperiment = experimentDao.findByName("Test Experiment");

        // Then
        assertNotNull(foundExperiment);
        assertEquals("Test Experiment", foundExperiment.getName());
    }




    void existsByName(){
        //Given
        String existName = "existingName";
        String nonExistName = "nonExistName";

        //When
        boolean existingNameResult = experimentDao.existsByName(existName);
        boolean nonExistingNameResult = experimentDao.existsByName(nonExistName);

        //Then
        assertTrue(existingNameResult, "Experiment with existing name should exist");
        assertTrue(nonExistingNameResult,"Experiment with non-existing name should not be exist");
    }


}
