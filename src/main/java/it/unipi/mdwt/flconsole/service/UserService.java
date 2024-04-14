package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.UserDao;
import it.unipi.mdwt.flconsole.dto.UserSummary;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.ValidatorAndSaver;

import javax.naming.AuthenticationException;

import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessTypeErrorsEnum;
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

    private final UserDao userDAO;
    private final MongoTemplate mongoTemplate;


    @Autowired
    public UserService(UserDao userDAO, MongoTemplate mongoTemplate) {
        this.userDAO = userDAO;
        this.mongoTemplate = mongoTemplate;
    }


    /**
     * Authenticate a user using their email and password.
     *
     * @param email    The email of the user attempting to authenticate.
     * @param password The password provided by the user.
     * @return The role of the authenticated user.
     * @throws AuthenticationException If the email or password format is invalid.
     * @throws BusinessException      If the user is not found.
     */
    public String authenticate(String email, String password) throws AuthenticationException, BusinessException {
        // Validate email and password using the Validator utility class
        if (!ValidatorAndSaver.validateEmail(email)) {
            throw new AuthenticationException("Invalid email format");
        }

        if (!ValidatorAndSaver.validatePassword(password)) {
            throw new AuthenticationException("Invalid password format");
        }

        // Search for the user by email and password in the database
        User user = userDAO.findRoleByEmailAndPassword(email, password);

        // If user is not found, throw BusinessException
        if (user == null) {
            throw new BusinessException(BusinessTypeErrorsEnum.NOT_FOUND);
        } else {
            // If user is found, return their role
            return user.getRole();
        }
    }



    /**
     * Registers a new user with the provided email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @throws BusinessException If an error occurs in the data access layer.
     * @throws AuthenticationException If the provided email or password is invalid.
     */
    public void signUp(String email, String password) throws BusinessException, AuthenticationException {
        User user = new User();

        // Validate email and password using the Validator utility class
        if (!ValidatorAndSaver.validateEmail(email)) {
            throw new AuthenticationException("Invalid email");
        }
        if (!ValidatorAndSaver.validatePassword(password)) {
            throw new AuthenticationException("Invalid password");
        }
        user.setEmail(email);
        user.setPassword(password);

        if (!userDAO.existsByEmail(email)) {
            userDAO.save(user);
        } else {
            throw new BusinessException(BusinessTypeErrorsEnum.DUPLICATED_ELEMENT);
        }
    }


    /**
     * Deletes the user account associated with the given email.
     *
     * @param email The email of the user account to delete.
     */
    public void deleteAccount(String email) {
        userDAO.deleteByEmail(email);
    }

    /**
     * Retrieves the user details for the given email.
     *
     * @param email The email of the user to retrieve.
     * @return The user details for the specified email.
     */
    public User getUser(String email) {
        return userDAO.findByEmail(email);
    }

    /**
     * Updates the user profile information based on the provided update request.
     *
     * @param email The email of the user whose profile to update.
     * @param updateRequest The update request containing the new profile information.
     */
    public void updateUserProfile(String email, UserSummary updateRequest) {
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

    /**
     * Checks if the user with the given email is the author of the specified experiment.
     *
     * @param email The email of the user to check.
     * @param experimentId The ID of the experiment to check.
     * @return True if the user is the author of the experiment, false otherwise.
     */
    public boolean isExperimentAuthor(String email, String experimentId) {
        return userDAO.existsUserByEmailAndExperimentId(email, experimentId);
    }

}
