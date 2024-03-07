package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.dao.UserDAO;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.utils.Validator;

import javax.naming.AuthenticationException;

import it.unipi.mdwt.flconsole.utils.exceptions.dao.DaoException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for handling authentication operations.
 */
@Service
public class UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    /**
     * Stub method simulating login through a DAO (Data Access Object).
     *
     * @return true if the login is successful (stub implementation).
     * @throws AuthenticationException if authentication fails.
     */
    public boolean STUBLoginDAO() throws AuthenticationException {
        return true;
    }
    public boolean STUBRegistrationDAO() throws AuthenticationException {
        return true;
    }


    //TODO: Implement register method
    public void register(String email, String password) throws AuthenticationException {
        // Validate email and password using the Validator utility class
        if (Validator.validateEmail(email)) {
            throw new AuthenticationException("Invalid email");
        }
        if (Validator.validatePassword(password)) {
            throw new AuthenticationException("Invalid password");
        }
        try {
            if (!STUBRegistrationDAO())
                throw new AuthenticationException("Invalid credentials");
        }catch (AuthenticationException e) {
            throw new AuthenticationException("An error occurred");
        }
    }

    /**
     * Authenticates a user based on the provided email and password.
     *
     * @param email    The email of the user.
     * @param password The password of the user.
     * @throws AuthenticationException if authentication fails.
     */
    public void authenticate(String email, String password) throws AuthenticationException {
        // Validate email and password using the Validator utility class
        if (!Validator.validateEmail(email)) {
            throw new AuthenticationException("Invalid email");
        }

        if (!Validator.validatePassword(password)) {
            throw new AuthenticationException("Invalid password");
        }

        if(!userDAO.existsByEmailAndPassword(email, password)) throw new AuthenticationException("Invalid credentials");

    }


    public void signUp(String email, String password) throws AuthenticationException {
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
            throw new AuthenticationException("User already exists");
        }
    }
}
