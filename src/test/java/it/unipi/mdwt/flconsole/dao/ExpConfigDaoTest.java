package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class ExpConfigDaoTest {

    @Autowired
    private ExpConfigDao expConfigDao;

    @Test
    void saveAndFindById() {
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

        expConfig.setCreationDate(LocalDate.now());

        // When
        ExpConfig savedConfig = expConfigDao.save(expConfig);
        Optional<ExpConfig> foundConfig = expConfigDao.findById(savedConfig.getId());

        // Then
        assertNotNull(foundConfig.orElse(null));
        assertEquals("TestConfig", foundConfig.get().getName());
        assertEquals("TestAlgorithm", foundConfig.get().getAlgorithm());
        assertEquals("TestStrategy", foundConfig.get().getStrategy());
        assertEquals(10, foundConfig.get().getNumClients());
        assertEquals("TestStopCondition", foundConfig.get().getStopCondition());
        assertEquals(0.5, foundConfig.get().getThreshold());

        assertTrue(foundConfig.get().getParameters().entrySet().containsAll(parametersList.entrySet()));
        assertTrue(parametersList.entrySet().containsAll(foundConfig.get().getParameters().entrySet()));


        assertEquals(LocalDate.now(), foundConfig.get().getCreationDate());
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
