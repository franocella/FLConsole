package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpConfig;
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
    ExpConfigDaoTest(ExpConfigDao expConfigDao, MongoTemplate mongoTemplate) {
        this.expConfigDao = expConfigDao;
        this.mongoTemplate = mongoTemplate;
    }


    @Test
    void saveAndRetrieve() {
        // Given
        ExpConfig expConfig = new ExpConfig();
        expConfig.setName("TestConfig");
        expConfig.setAlgorithm("TestAlgorithm");
        expConfig.setStrategy("TestStrategy");
        expConfig.setNumClients(10);
        expConfig.setStopCondition("TestStopCondition");
        expConfig.setThreshold(0.5);

        Map<String, String> parametersList = new HashMap<>(Map.of("param1", "value1", "param2", "value2"));
        expConfig.setParameters(parametersList);

        // When
        ExpConfig savedExpConfig = expConfigDao.save(expConfig);

        // Then
        assertNotNull(savedExpConfig.getId(), "ID should not be null after save");

        // Retrieve the saved ExpConfig from the repository
        Optional<ExpConfig> retrievedExpConfigOptional = expConfigDao.findById(savedExpConfig.getId());

        // Assert that the retrieved ExpConfig matches the original one
        assertTrue(retrievedExpConfigOptional.isPresent(), "Saved ExpConfig should be present");
        ExpConfig retrievedExpConfig = retrievedExpConfigOptional.get();
        assertEquals(expConfig.getName(), retrievedExpConfig.getName(), "Name should match");
        assertEquals(expConfig.getAlgorithm(), retrievedExpConfig.getAlgorithm(), "Algorithm should match");
        assertEquals(expConfig.getStrategy(), retrievedExpConfig.getStrategy(), "Strategy should match");
        assertEquals(expConfig.getNumClients(), retrievedExpConfig.getNumClients(), "NumClients should match");
        assertEquals(expConfig.getStopCondition(), retrievedExpConfig.getStopCondition(), "StopCondition should match");
        assertEquals(expConfig.getThreshold(), retrievedExpConfig.getThreshold(), "Threshold should match");
        assertEquals(expConfig.getParameters(), retrievedExpConfig.getParameters(), "Parameters should match");
    }



    @Test
    void update() {
        // Given
        ExpConfig expConfig = new ExpConfig();
        expConfig.setName("UpdateConfig");
        ExpConfig savedConfig = expConfigDao.save(expConfig);

        // When
        savedConfig.setName("UpdatedConfig");
        expConfigDao.save(savedConfig);

        // Then
        Optional<ExpConfig> updatedConfig = expConfigDao.findById(savedConfig.getId());
        assertTrue(updatedConfig.isPresent());
        assertEquals("UpdatedConfig", updatedConfig.get().getName());
    }

    @Test
    void delete() {
        // Given
        ExpConfig expConfig = new ExpConfig();
        expConfig.setName("DeleteConfig");
        ExpConfig savedConfig = expConfigDao.save(expConfig);

        // When
        expConfigDao.deleteById(savedConfig.getId());

        // Then
        Optional<ExpConfig> deletedConfig = expConfigDao.findById(savedConfig.getId());
        assertFalse(deletedConfig.isPresent());
    }
}