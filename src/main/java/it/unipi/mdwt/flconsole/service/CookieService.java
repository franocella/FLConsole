package it.unipi.mdwt.flconsole.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import static it.unipi.mdwt.flconsole.utils.Cryptography.decrypt;
import static it.unipi.mdwt.flconsole.utils.Cryptography.encrypt;

@Service
public class CookieService {

    // Secret key for encryption and decryption

    public void setCookie(String cookieName, String value, HttpServletResponse response) {
        // Encrypt the cookie value
        String encryptedValue = encrypt(value);

        // Create and set the cookie with the encrypted value
        Cookie myCookie = new Cookie(cookieName, encryptedValue);
        myCookie.setMaxAge(3600); // Cookie will expire in 1 hour (in seconds)
        myCookie.setPath("/");    // Cookie is accessible to the entire application
        response.addCookie(myCookie);
    }

    public String getCookieValue(Cookie[] cookies, String cookieName) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    // Decrypt the cookie value before returning it
                    return decrypt(cookie.getValue());
                }
            }
        }
        return null;
    }

    public void deleteCookie(String cookieName, HttpServletResponse response) {
        Cookie myCookie = new Cookie(cookieName, "");
        myCookie.setMaxAge(0); // Cookie will expire immediately
        myCookie.setPath("/"); // Cookie is accessible to the entire application
        response.addCookie(myCookie);
    }


}