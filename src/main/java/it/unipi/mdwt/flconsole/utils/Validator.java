package it.unipi.mdwt.flconsole.utils;

public class Validator {
    public static boolean validateEmail(String email) {
        return email != null && !email.isEmpty() && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    public static boolean validatePassword(String password) {
        return password != null && !password.isEmpty() && password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$");
    }
}
