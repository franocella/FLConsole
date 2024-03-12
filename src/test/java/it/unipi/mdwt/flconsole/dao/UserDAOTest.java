package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains JUnit tests for the UserDAO class.
 */
@DataMongoTest
class UserDAOTest {

    @Autowired
    private UserDAO userRepository;



    /**
     * Test the save method of the UserDAO.
     */
    @Test
    void save() {
        // Given
        User user = new User();
        user.setEmail("saveTest@example.com");
        user.setPassword("password");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("saveTest@example.com", savedUser.getEmail());
        assertEquals("password", savedUser.getPassword());
/*
        assertNull(savedUser.getConfigurations()); // Expect configurations to be null or empty
        assertNull(savedUser.getExperiments()); // Expect experiments to be null or empty*/

        // Given - Another user with the same email
        User duplicateUser = new User();
        duplicateUser.setEmail("saveTest@example.com");
        duplicateUser.setPassword("newPassword");

        // When - Try to save another user with the same email
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(duplicateUser));
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
        String emailToDelete = "updateTest@example.com";
        User user = userRepository.findByEmail(emailToDelete);

        // Ensure that the user exists before attempting deletion
        assertNotNull(user);

        // When
        userRepository.deleteByEmail(emailToDelete);

        // Then
        assertFalse(userRepository.existsByEmail(emailToDelete));
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
}
