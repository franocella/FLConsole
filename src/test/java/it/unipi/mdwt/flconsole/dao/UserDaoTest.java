package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.dto.ExperimentSummary;
import it.unipi.mdwt.flconsole.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains JUnit tests for the UserDAO class.
 */
@DataMongoTest
class UserDaoTest {

    @Autowired
    private UserDao userRepository;

    @Autowired
    private ExperimentDao experimentDao;



    /**
     * Test the save method of the UserDAO.
     */
    @Test
    void save() {
        User user = new User();
        user.setEmail("example2@mail.com");
        user.setPassword("examplePsw");
        List<String> configurations = new ArrayList<>();
        user.setConfigurations(configurations);
        user.setRole("exampleRole");

        // Check if the email is already associated with another user
        User existingUserOptional = userRepository.findByEmail(user.getEmail());
        if (existingUserOptional!=null) {
            System.out.println("User with this email already exists");
            return; // Exit the method without saving
        }

        // Fetching Experiment from repository
        Optional<Experiment> experimentOptional = experimentDao.findById("65f6f495b9143e510574da43");
        if (experimentOptional.isPresent()) {
            Experiment experiment = experimentOptional.get();
            // Creating ExperimentSummary object
            ExperimentSummary experimentSummary = new ExperimentSummary();
            experimentSummary.setId(experiment.getId());
            experimentSummary.setName(experiment.getName());
            experimentSummary.setConfigName(experiment.getExpConfig().getName());
            experimentSummary.setCreationDate(experiment.getCreationDate());
            // Creating a list of ExperimentSummary and adding the single ExperimentSummary to it
            List<ExperimentSummary> experiments = new ArrayList<>();
            experiments.add(experimentSummary);
            // Setting the list of ExperimentSummary to the user
            user.setExperiments(experiments);
        } else {
            System.out.println("Experiment with that id not found");
        }

        // Saving user to repository if the email is not already in use
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("example@mail.com", savedUser.getEmail());

        System.out.println("User is created");
    }

    /**
     * Test the update method of the UserDAO.
     */
    @Test
    void update() {
        // Given
        User user = new User();
        user.setEmail("updateTest@example.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        // When
        savedUser.setPassword("newPassword");
        userRepository.save(savedUser);

        // Then
        User updatedUser = userRepository.findById(String.valueOf(savedUser.getId())).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("newPassword", updatedUser.getPassword());
        /*
        assertNull(updatedUser.getConfigurations()); // Expect configurations to be null or empty
        assertNull(updatedUser.getExperiments()); // Expect experiments to be null or empty
        */
    }

    /**
     * Test the delete method of the UserDAO.
     */
    @Test
    void delete() {
        // Given
        String emailToDelete = "example2@mail.com";
        User user = userRepository.findByEmail(emailToDelete);

        // Ensure that the user exists before attempting deletion
        assertNotNull(user);

        List<ExperimentSummary> experimentSummaries = user.getExperiments();
        if (experimentSummaries!= null){
            for (ExperimentSummary experimentSummary : experimentSummaries){
                Optional<Experiment> experiment = experimentDao.findById(experimentSummary.getId());
                experimentDao.deleteById(experimentSummary.getId());

                assertNotNull(experimentSummary);
                assertNotNull(experiment);
            }
        }
        // When
        userRepository.deleteByEmail(emailToDelete);

        // Then
        assertFalse(userRepository.existsByEmail(emailToDelete));
    }

    /**
     * Test the findByEmail method of the UserDAO.
     */
    @Test
    void findByEmail() {
        // Given
        User user = new User();
        // When
        User foundUser = userRepository.findByEmail("firstTest@example.com");

        // Then
        assertNotNull(foundUser);
        assertEquals("firstTest@example.com", foundUser.getEmail());
    }



    @Test
    void existsByEmail() {
        // Given
        String existingEmail = "firstTest@example.com";
        String nonExistingEmail = "nonExisting@example.com";

        // When
        boolean existingEmailResult = userRepository.existsByEmail(existingEmail);
        boolean nonExistingEmailResult = userRepository.existsByEmail(nonExistingEmail);

        // Then
        assertTrue(existingEmailResult, "User with existing email should exist");
        assertFalse(nonExistingEmailResult, "User with non-existing email should not exist");
    }

    @Test
    void existsByEmailAndPassword() {
        // Given
        String existingEmail = "firstTest@example.com";
        String correctPassword = "P@ssw0rd";
        String incorrectPassword = "wrongPassword";

        // When
        boolean correctCredentialsResult = userRepository.existsByEmailAndPassword(existingEmail, correctPassword);
        boolean incorrectCredentialsResult = userRepository.existsByEmailAndPassword(existingEmail, incorrectPassword);

        // Then
        assertTrue(correctCredentialsResult, "User with correct credentials should exist");
        assertFalse(incorrectCredentialsResult, "User with incorrect credentials should not exist");
    }

    @Test
    void saveAdmin() {
        // Given
        String adminEmail = "admin@example.com";
        String adminPassword = "AdminP@ss";
        String adminRole = "admin";

        // When
        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setPassword(adminPassword);
        adminUser.setRole(adminRole);

        // Then
        assertDoesNotThrow(() -> {
            User savedAdmin = userRepository.saveWithException(adminUser);

            assertNotNull(savedAdmin.getId());
            assertEquals(adminEmail, savedAdmin.getEmail());
            assertEquals(adminPassword, savedAdmin.getPassword());
            assertEquals(adminRole, savedAdmin.getRole());

            // Check if the saved user can be retrieved by email and password
            User retrievedAdmin = userRepository.findRoleByEmailAndPassword(adminEmail, adminPassword);
            assertEquals(adminRole, retrievedAdmin.getRole());
        });
    }


    @Test
    void findRoleByEmailAndPasswordForAdmin() {
        // Given
        String adminEmail = "admin@example.com";
        String adminPassword = "AdminP@ss";

        // When
        User retrievedAdmin = userRepository.findRoleByEmailAndPassword(adminEmail, adminPassword);

        // Then
        assertNotNull(retrievedAdmin);
        assertEquals("admin", retrievedAdmin.getRole());  // Assuming "admin" is the expected role
    }

    @Test
    void findListOfConfigurationsByEmail() {
        // Given
        String userEmail = "admin@example.com";

        // When
        List<String> retrievedConfigurations = userRepository.findListOfConfigurationsByEmail(userEmail);

        // Then
        assertNotNull(retrievedConfigurations);
        assertFalse(retrievedConfigurations.isEmpty());

        System.out.println(retrievedConfigurations);
    }

    @Test
    void existsUserByEmailAndExperimentId() {
        // Given
        String adminEmail = "admin@example.com";
        String experimentId = "65f7623ce52fdb3b082cf821";

        // When
        boolean userExists = userRepository.existsUserByEmailAndExperimentId(adminEmail, experimentId);

        // Then
        assertTrue(userExists);
        System.out.println("The experiment is created by the user");
    }

}
