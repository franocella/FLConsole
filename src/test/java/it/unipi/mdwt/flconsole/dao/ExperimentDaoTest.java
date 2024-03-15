package it.unipi.mdwt.flconsole.dao;


import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DataIntegrityViolationException;
import com.mongodb.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains JUnit tests for the ExperimentDAO class.
 */

@DataMongoTest
class ExperimentDaoTest {

    @Autowired
    private ExperimentDao experimentDao;

    /**
     * test to save method of the ExperimentDAO
     */
    @Test
    void save(){
        //Given
        Experiment experiment = new Experiment();
        experiment.setName("Test Experiment");
        ExpConfig config = new ExpConfig();
        config.setName("Test Config");
        experiment.setConfig(config);

        // When
        Experiment savedExperiment = experimentDao.save(experiment);

        // Then
        assertNotNull(savedExperiment.getId());
        assertEquals("Test Experiment", savedExperiment.getName());
        assertEquals("Test Config", savedExperiment.getConfig().getName());

        System.out.println("first exp created");

        // Given - Another experiment with the same name
        Experiment duplicatedExp = new Experiment();
        duplicatedExp.setName("Test Experiment");
        ExpConfig config2 = new ExpConfig();
        config2.setName("Test Config2");
        experiment.setConfig(config2);

        assertThrows(DataIntegrityViolationException.class, () -> experimentDao.save(duplicatedExp));

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

    /**
     * Test the findByConfig method of the ExperimentDao.
     */
    @Test
    void findByConfig(){
        // Given
        Experiment experiment = new Experiment();
        experiment.setName("Test Experiment");
        ExpConfig config = new ExpConfig();
        config.setName("Test Config");
        experiment.setConfig(config);

        // Save the experiment
        Experiment savedExperiment = experimentDao.save(experiment);

        // When
        Experiment foundExperiment = experimentDao.findByConfig(config);

        // Then
        assertNotNull(foundExperiment);
        assertEquals("Test Experiment", foundExperiment.getName());
        assertEquals("Test Config", foundExperiment.getConfig().getName());
    }

    /**
     * Test the update method of the ExperimentDAO.
     */
    @Test
    void update(){
        //Given
        Experiment experiment = new Experiment();
        experiment.setName("Test Experiment");
        ExpConfig config = new ExpConfig();
        config.setName("Test Config");
        experiment.setConfig(config);

        Experiment savedExperiment = experimentDao.save(experiment);

        //When
        savedExperiment.setName("newName");
        ExpConfig updatedConfig = new ExpConfig();
        updatedConfig.setName("Updated Config");
        savedExperiment.setConfig(updatedConfig);
        experimentDao.save(savedExperiment);

        //Then
        Experiment updateExp = experimentDao.findById(savedExperiment.getId()).orElse(null);
        assertNotNull(updateExp);
        assertEquals("Updated Experiment Name", updateExp.getName());
        assertEquals("Updated Config", updateExp.getConfig().getName());
    }

    /**
     * Test the delete method of the UserDAO.
     */
    @Test
    void delete(){
        //Given
        String nameToDelete = "exp1";
        Experiment experiment = experimentDao.findByName(nameToDelete);

        // Ensure that the user exists before attempting deletion
        assertNotNull(experiment);

        //When
        experimentDao.deleteByName(nameToDelete);

        //then
        assertFalse(experimentDao.existsByName(nameToDelete));
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
