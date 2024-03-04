package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.utils.Validator;

import javax.naming.AuthenticationException;

import org.springframework.stereotype.Service;

/**
 * Service class for handling authentication operations.
 */
@Service
public class UserService {

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
        if (Validator.validateEmail(email)) {
            throw new AuthenticationException("Invalid email");
        }
        if (Validator.validatePassword(password)) {
            throw new AuthenticationException("Invalid password");
        }

        // TODO: Implement DAO for real authentication
        try {
            if (!STUBLoginDAO()) {
                throw new AuthenticationException("Invalid credentials");
            }
        } catch (Exception e) {
            throw new AuthenticationException("An error occurred");
        }
    }
}
