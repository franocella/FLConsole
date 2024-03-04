package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDAO extends MongoRepository<User, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    // Custom queries
    User findByEmail(String email);
}