package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains JUnit tests for the UserDAO class.
 */
@DataMongoTest
class UserDAOTest {

    @Autowired
    private UserDAO userRepository;

    /**
     * Test the findByEmail method of the UserDAO.
     */
    @Test
    void findByEmail() {
        // Given
        User user = new User();
        // When
        User foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    /**
     * Test the save method of the UserDAO.
     */
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

        // Given - Another user with the same email
        User duplicateUser = new User();
        duplicateUser.setEmail("saveTest@example.com");
        duplicateUser.setPassword("newPassword");

        // When - Try to save another user with the same email
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(duplicateUser));
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
    }

    /**
     * Test the delete method of the UserDAO.
     */
    @Test
    void delete() {
        // Given
        User user = new User();
        user.setEmail("deleteTest@example.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        // When
        userRepository.deleteById(String.valueOf(savedUser.getId()));

        // Then
        assertFalse(userRepository.existsById(String.valueOf(savedUser.getId())));
    }
}
