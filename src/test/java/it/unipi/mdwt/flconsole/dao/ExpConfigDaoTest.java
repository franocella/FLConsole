package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.ExpConfigSummary;
import it.unipi.mdwt.flconsole.model.Experiment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class ExpConfigDaoTest {


    private final ExpConfigDao expConfigDao;
    private final MongoTemplate mongoTemplate;

    @Autowired
    private ExperimentDao experimentDao;

    @Autowired
    ExpConfigDaoTest(ExpConfigDao expConfigDao, MongoTemplate mongoTemplate) {
        this.expConfigDao = expConfigDao;
        this.mongoTemplate = mongoTemplate;
    }


    @Test
    void saveAndRetrieve() {
        // Given
        ExpConfig expConfig = new ExpConfig();
        expConfig.setName("TestConfig2");
        expConfig.setAlgorithm("fcmeans");
        expConfig.setCodeLanguage("python");
        expConfig.setClientSelectionStrategy("probability");
        expConfig.setClientSelectionRatio(1.0);
        expConfig.setMinNumberClients(0);
        expConfig.setMaxNumberOfRounds(5);
        expConfig.setStopCondition("max_number_rounds");
        expConfig.setStopConditionThreshold(5.0);

        Map<String, String> parametersList = new HashMap<>(Map.of("numFeatures", "16", "numClusters", "10",
                "targetFeature", "16", "lambdaFactor", "2", "seed", "10"));
        expConfig.setParameters(parametersList);

        // When
        ExpConfig savedExpConfig = expConfigDao.save(expConfig);

        /*// Then
        assertNotNull(savedExpConfig.getId(), "ID should not be null after save");

        // Retrieve the saved ExpConfig from the repository
        Optional<ExpConfig> retrievedExpConfigOptional = expConfigDao.findById(savedExpConfig.getId());

        // Assert that the retrieved ExpConfig matches the original one
        assertTrue(retrievedExpConfigOptional.isPresent(), "Saved ExpConfig should be present");
        ExpConfig retrievedExpConfig = retrievedExpConfigOptional.get();
        assertEquals(expConfig.getName(), retrievedExpConfig.getName(), "Name should match");
        assertEquals(expConfig.getAlgorithm(), retrievedExpConfig.getAlgorithm(), "Algorithm should match");
        assertEquals(expConfig.getClientSelectionStrategy(), retrievedExpConfig.getClientSelectionStrategy(), "Strategy should match");
        //assertEquals(expConfig.getNumClients(), retrievedExpConfig.getNumClients(), "NumClients should match");
        assertEquals(expConfig.getStopCondition(), retrievedExpConfig.getStopCondition(), "StopCondition should match");
        // assertEquals(expConfig.getThreshold(), retrievedExpConfig.getThreshold(), "Threshold should match");
        assertEquals(expConfig.getParameters(), retrievedExpConfig.getParameters(), "Parameters should match");*/
    }


    @Test
    void update() {
        // Given
        ExpConfig expConfig = new ExpConfig();
        expConfig.setName("UpdateConfig");
        ExpConfig savedConfig = expConfigDao.save(expConfig);

        List<Experiment> experiments = experimentDao.findAll();
        for (Experiment experiment: experiments){
            ExpConfigSummary expConfigSummary = experiment.getExpConfig();
            if (expConfigSummary != null && expConfigSummary.getId().equals(expConfig.getId())) {
                expConfigSummary.setName(savedConfig.getName());
                experimentDao.save(experiment);
            }
        }

        // When
        savedConfig.setName("UpdatedConfig2");
        expConfigDao.save(savedConfig);

        // Then
        Optional<ExpConfig> updatedConfig = expConfigDao.findById(savedConfig.getId());
        assertTrue(updatedConfig.isPresent());
        assertEquals("UpdatedConfig2", updatedConfig.get().getName());
    }

    @Test
    void delete() {
        String nameToDelete = "TestConfig2";
        ExpConfig expConfig = expConfigDao.findByName(nameToDelete);

        assertNotNull(expConfig);

        List<Experiment> experiments = experimentDao.findAll();
        for (Experiment experiment: experiments){
            ExpConfigSummary expConfigSummary = experiment.getExpConfig();
            if (expConfigSummary != null && expConfigSummary.getId().equals(expConfig.getId())) {
                // Remove the expConfigSummary if it matches the ID of the ExpConfig being deleted
                experiment.setExpConfig(null);
                experimentDao.save(experiment);
            }
        }

        expConfigDao.deleteByName(nameToDelete);

        assertFalse(expConfigDao.existsByName(nameToDelete));

        // Verify that no experiments reference the deleted ExpConfig
        List<Experiment> updatedExperiments = experimentDao.findAll();
        for (Experiment experiment : updatedExperiments) {
            ExpConfigSummary expConfigSummary = experiment.getExpConfig();
            //assertNull(expConfigSummary);
        }
    }


}
