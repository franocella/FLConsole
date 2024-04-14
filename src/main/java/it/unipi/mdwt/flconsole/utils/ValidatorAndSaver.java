package it.unipi.mdwt.flconsole.utils;

import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static it.unipi.mdwt.flconsole.utils.Constants.PROJECT_PATH;

public class ValidatorAndSaver {

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

    /**
     * Saves a file containing model data.
     *
     * @param byteArray The byte array representing the file contents.
     * @param expId The ID of the experiment associated with the file.
     * @return The relative file path where the file is saved.
     * @throws IOException If an I/O error occurs while saving the file.
     */
    public static String saveFile(byte[] byteArray, String expId) throws IOException {
        // Generates a unique name for the file
        String modelName = "exp_" + expId + ".bin";

        // Create the full path for saving the file
        Path filePath = Paths.get(PROJECT_PATH, "FL_models", modelName);

        // Ensure the directory exists, otherwise create the directory
        Files.createDirectories(filePath.getParent());

        // Write byte array to file
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(byteArray);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filePath, e);
        }

        // Extract relative path
        String base = Paths.get(PROJECT_PATH).toString();

        // Return the relative file path as a string
        return filePath.toString().substring(base.length());
    }
}
