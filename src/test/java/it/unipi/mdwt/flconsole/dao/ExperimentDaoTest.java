package it.unipi.mdwt.flconsole.dao;


import it.unipi.mdwt.flconsole.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

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
        experiment.setName("Save Test Experiment2");


        Optional<ExpConfig> config = expConfigDao.findById("65f6f47b88c22a662c1da8f7");


        //if config exist then get the parameter create the config summary
        if (config.isPresent()){
            ExpConfig expConfig = config.get();
            ExpConfigSummary expConfigSummary = new ExpConfigSummary();
            expConfigSummary.setId(expConfig.getId());
            expConfigSummary.setName(expConfig.getName());
            expConfigSummary.setAlgorithm(expConfig.getAlgorithm());
            experiment.setExpConfig(expConfigSummary);
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

        // Fetch all users
        List<User> users = userDao.findAll();
        for (User user : users) {
            // Check if the user has experiments associated with the updated experiment
            List<ExperimentSummary> updatedExperiments = user.getExperiments();
            if (updatedExperiments != null) {
                // Iterate over the user's experiments
                for (ExperimentSummary exp : updatedExperiments) {
                    // If the experiment ID matches, update the name
                    if (exp.getId().equals(savedExperiment.getId())) {
                        exp.setName(savedExperiment.getName());
                        break; // No need to continue checking once the experiment is found
                    }
                }
                // Save the updated user
                userDao.save(user);
            }
        }
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
       String nameToDelete = "Save Test Experiment2";
       Experiment experiment = experimentDao.findByName(nameToDelete);

       assertNotNull(experiment);


       ExpConfigSummary expConfigSummary = experiment.getExpConfig();
       Optional<ExpConfig> expConfig = expConfigDao.findById(expConfigSummary.getId());

        // Get the users associated with the experiment
        List<User> users = userDao.findAll();
        for (User user: users){
            List<ExperimentSummary> experiments = user.getExperiments();
            experiments.removeIf(exp -> exp.getId().equals(experiment.getId()));
            user.setExperiments(experiments);
            userDao.save(user);
        }

       experimentDao.deleteByName(nameToDelete);


        // Check if the experiment is deleted
        assertFalse(experimentDao.existsByName(nameToDelete));


    }

    /**
     * Test the findByName method of the ExperimentDao.
     */
    @Test
    void findByName(){
        // Given
        Experiment experiment = new Experiment();
        experiment.setName("Test Experiment");
        Experiment savedExperiment = experimentDao.save(experiment);


        // When
        Experiment foundExperiment = experimentDao.findByName("Test Experiment");

        // Then
        assertNotNull(foundExperiment);
        assertEquals("Test Experiment", foundExperiment.getName());
    }

    @Test
    void findByExpConfigName(){
        Experiment experiment = new Experiment();
        experiment.setName("name");
        ExpConfigSummary expConfigSummary = new ExpConfigSummary();
        expConfigSummary.setName("test name");
        experiment.setExpConfig(expConfigSummary);
        Experiment savedExp = experimentDao.save(experiment);

        Experiment foundExp = experimentDao.findByExpConfigName("test name");

        assertNotNull(foundExp);
        assertEquals("test name",foundExp.getName());
    }


    @Test
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

    @Test
    void findByCreationDate(){
        // Given
        Date creationDate = new Date();
        Experiment experiment = new Experiment();
        experiment.setName("Test Experiment");
        experiment.setCreationDate(creationDate);
        Experiment savedExperiment = experimentDao.save(experiment);

        // When
        Experiment foundExperiment = experimentDao.findByCreationDate(creationDate);

        // Then
        assertNotNull(foundExperiment);
        assertEquals("Test Experiment", foundExperiment.getName());
        assertEquals(creationDate, foundExperiment.getCreationDate());
    }

}
