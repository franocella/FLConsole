package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDao;
import it.unipi.mdwt.flconsole.model.Experiment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {


    private final UserDao userDAO;
    private final ExperimentDao experimentDao;

    @Autowired
    UserServiceTest(UserDao userDAO, ExperimentDao experimentDao) {
        this.userDAO = userDAO;
        this.experimentDao = experimentDao;
    }

    @Test
    void getExperimentsForUser() {
        String userEmail = "firstTest@example.com";
        List<String> experimentsList = userDAO.findListOfConfigurationsByEmail(userEmail);
        assertNotNull(experimentsList);
        System.out.println(experimentsList);
        List<Experiment> experiments = experimentDao.findAllById(experimentsList);
        assertNotNull(experiments);
        System.out.println(experiments);


    }
}