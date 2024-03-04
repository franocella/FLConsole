package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class UserDAOTest {

    @Autowired
    private UserDAO userRepository; // Assuming UserRepository is your DAO

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

    @Test
    void save() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("password", savedUser.getPassword());
    }

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
