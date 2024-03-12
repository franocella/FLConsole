package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import org.junit.jupiter.api.Test;

import static it.unipi.mdwt.flconsole.service.ExperimentService.startExperiment;
import static org.junit.jupiter.api.Assertions.*;

class ExperimentServiceTest {

    @Test
    void startExperimentTest() {
        ExpConfig expConfig = new ExpConfig();
        expConfig.setName("TestConfig");
        expConfig.setAlgorithm("FedAvg");
        expConfig.setStrategy("Federated");
        expConfig.setNumClients(10);
        expConfig.setStopCondition("Rounds");
        expConfig.setThreshold(0.1);
        expConfig.getParameters().put("learning_rate", "0.01");
        expConfig.getParameters().put("epochs", "10");
        expConfig.getParameters().put("batch_size", "32");
        startExperiment(expConfig, "id1");
    }
}