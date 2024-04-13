package it.unipi.mdwt.flconsole.utils;

import org.springframework.util.StringUtils;

public class Validator {

    /**
     * Validates the format of an email address.
     * The email address must adhere to the standard format rules to be considered valid.
     * The email address should conform to the following rules:
     * it must not be empty or null;
     * it should consist of a local part, followed by the '@' symbol, and a domain part;
     * the local part can contain letters (both uppercase and lowercase), digits, and special characters
     *  such as '_', '.', '%', '+', and '-'. It must start and end with a letter or digit;
     * the domain part should consist of letters, digits, and hyphens, separated by periods;
     * the domain extension (e.g., 'com', 'org') should have between 2 and 6 characters;
     * the entire email address should match the pattern:
     *  "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".
     * @param email The email address to be validated.
     * @return {@code true} if the email address is valid; otherwise, {@code false}.
     * Example of a valid email address: "john.doe@example.com"
     */
    public static boolean validateEmail(String email) {
        return StringUtils.hasText(email) && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    /**
     * Validates the format of a password.
     * The password must meet the following criteria to be considered valid:
     * it should not be empty or null;
     * it must contain at least one digit (0-9);
     * it must contain at least one lowercase letter (a-z);
     * it must contain at least one uppercase letter (A-Z);
     * it must contain at least one special character from the set: !@#$%^&*()-=_+[]{}|;:'",.<>?/\\;
     * it should be at least 8 characters long;
     * it should not contain whitespaces.
     *
     * @param password The password to be validated.
     * @return {@code true} if the password is valid; otherwise, {@code false}.
     * Example of a valid password: "P@ssw0rd"
     */
    public static boolean validatePassword(String password) {
        return StringUtils.hasText(password) && password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()-=_+\\[\\]{}|;:'\",.<>?/\\\\])(?=\\S+$).{8,}$");
    }

    public static String createCookieName(String value) {
        return value + "_cookie";
    }
}
