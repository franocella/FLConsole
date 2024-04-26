package it.unipi.mdwt.flconsole.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import it.unipi.mdwt.flconsole.service.CookieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class HttpInterceptor implements HandlerInterceptor {

    private static final Set<String> ALLOWED_URIS = Set.of("/FLConsole/login", "/FLConsole/signup", "/FLConsole/CSS", "/FLConsole/JS", "/FLConsole/Images", "/CSS", "/JS", "/Images");

    private final CookieService cookieService;

    @Autowired
    public HttpInterceptor(CookieService cookieService) {
        this.cookieService = cookieService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (ALLOWED_URIS.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        String emailCookie = cookieService.getCookieValue(request.getCookies(), "email");

        if (emailCookie != null) {
            // Check if the requested page is an admin page
            if (uri.startsWith("/FLConsole/admin")) {
                // If it's an admin page, also check for the "role" cookie
                String roleCookie = cookieService.getCookieValue(request.getCookies(), "role");
                if (roleCookie != null && roleCookie.equals("admin")) {
                    return true;
                }
                // If "role" cookie is not present or not equal to "admin", redirect to access denied page
                response.sendRedirect("/FLConsole/access-denied");
                return false;
            }
            return true;
        }

        response.sendRedirect("/FLConsole/login");
        return false;
    }
}
