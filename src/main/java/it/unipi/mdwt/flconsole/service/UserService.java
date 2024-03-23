package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDAO;
import it.unipi.mdwt.flconsole.dto.UserDTO;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.Validator;

import javax.naming.AuthenticationException;

import it.unipi.mdwt.flconsole.utils.exceptions.dao.DaoException;
import it.unipi.mdwt.flconsole.utils.exceptions.dao.DaoTypeErrorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for handling authentication operations.
 */
@Service
public class UserService {

    private final UserDAO userDAO;
    private final ExperimentDao experimentDao;
    private final MongoTemplate mongoTemplate;


    @Autowired
    public UserService(UserDAO userDAO, ExperimentDao experimentDao, MongoTemplate mongoTemplate) {
        this.userDAO = userDAO;
        this.experimentDao = experimentDao;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Authenticates a user based on the provided email and password.
     *
     * @param email    The email of the user.
     * @param password The password of the user.
     * @throws AuthenticationException if authentication fails.
     * @return The role of the user if authentication is successful.
     */
    public Optional<String> authenticate(String email, String password) throws AuthenticationException {
        // Validate email and password using the Validator utility class
        if (!Validator.validateEmail(email)) {
            throw new AuthenticationException("Invalid email format");
        }

        if (!Validator.validatePassword(password)) {
            throw new AuthenticationException("Invalid password format");
        }

        try {
            User user = userDAO.findRoleByEmailAndPasswordWithException(email, password);
            if (user != null) {
                return Optional.ofNullable(user.getRole());
            } else {
                throw new AuthenticationException("User not found");
            }
        } catch (DaoException e) {
            throw new AuthenticationException("User not found");
        }
    }

    public void signUp(String email, String password) throws DaoException, AuthenticationException {
        User user = new User();

        // Validate email and password using the Validator utility class
        if (!Validator.validateEmail(email)) {
            throw new AuthenticationException("Invalid email");
        }
        if (!Validator.validatePassword(password)) {
            throw new AuthenticationException("Invalid password");
        }
        user.setEmail(email);
        user.setPassword(password);
        try {
            userDAO.saveWithException(user);
        } catch (DaoException e) {
            throw new DaoException(DaoTypeErrorsEnum.DUPLICATED_ELEMENT);
        }
    }

    public void deleteAccount(String email) {
        userDAO.deleteByEmail(email);
    }


    public User getUser(String email) {
        return userDAO.findByEmail(email);
    }

    public void updateUserProfile(String email, UserDTO updateRequest) {
        Query query = new Query(Criteria.where("email").is(email));
        Update update = new Update();

        if (updateRequest.getEmail() != null) {
            update.set("email", updateRequest.getEmail());
        }
        if (updateRequest.getPassword() != null) {
            update.set("password", updateRequest.getPassword());
        }
        if (updateRequest.getDescription() != null) {
            update.set("description", updateRequest.getDescription());
        }
        mongoTemplate.updateFirst(query, update, User.class);
    }

}
