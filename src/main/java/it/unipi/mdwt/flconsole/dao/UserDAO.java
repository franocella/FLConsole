package it.unipi.mdwt.flconsole.dao;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.User;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Repository
public interface UserDAO extends MongoRepository<User, String> {
    // No need to provide implementations for CRUD methods,
    // Spring Data MongoDB will automatically generate them.

    // Custom queries
    User findByEmail(String email);
    void deleteByEmail(String email);
    Boolean existsByEmail(String email);

    /*List<ExpConfig> findExpConfigsByConfigurations_Email(String userEmail);*/

}