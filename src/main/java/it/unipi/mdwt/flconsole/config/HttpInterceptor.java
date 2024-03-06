package it.unipi.mdwt.flconsole.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Set;

@Component
public class HttpInterceptor implements HandlerInterceptor {

    private static final Set<String> ALLOWED_URIS = Set.of("/login", "/signup", "/CSS", "/JS", "/Images");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (ALLOWED_URIS.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("email")) {
                    return true;
                }
            }
        }

        response.sendRedirect("/login");
        return false;
    }
}
