package it.unipi.mdwt.flconsole.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.service.CookieService;
import it.unipi.mdwt.flconsole.service.ExpConfigService;
import it.unipi.mdwt.flconsole.service.UserService;
import it.unipi.mdwt.flconsole.service.ExperimentService;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Controller
public class MainController {

    private final UserService userService;
    private final ExperimentService experimentService;
    private final ExpConfigService expConfigService;
    private final Logger applicationLogger;

    private final CookieService cookieService;
    private final ObjectMapper objectMapper;



    @Autowired
    public MainController(UserService userService, ExperimentService experimentService, ExpConfigService expConfigService, Logger applicationLogger, CookieService cookieService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.experimentService = experimentService;
        this.expConfigService = expConfigService;
        this.applicationLogger = applicationLogger;
        this.cookieService = cookieService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/login")
    public String loginGET() {
        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginPOST(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            Optional<String> roleOptional = userService.authenticate(email, password);
            // Authentication successful, set cookies
            cookieService.setCookie("email", email, response);
            if (roleOptional.isPresent()) {
                String role = roleOptional.get();
                cookieService.setCookie("role", role, response);
            }
            return ResponseEntity.ok("{\"status\": \"success\"}");
        } catch (AuthenticationException e) {
            // Return an error JSON response
            applicationLogger.severe("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/logout")
    public String logoutGET(HttpServletResponse response) {
        cookieService.deleteCookie("email", response);
        return "redirect:/login";
    }
    @GetMapping("/signup")
    public String signUp() {
        return "signup";
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            userService.signUp(email, password);

            // Authentication successful, set cookie
            cookieService.setCookie("email", email, response);

            // Return a success JSON response
            return ResponseEntity.ok("{\"status\": \"success\"}");
        } catch (AuthenticationException e) {
            // Return an error JSON response
            applicationLogger.severe("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/")
    public String home(Model model, @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // Retrieve recent experiments from the service using pagination
            Page<Experiment> recentExperimentsPage = experimentService.getRecentExperiments(page, pageSize);

            // Add the paginated list of recent experiments to the model
            model.addAttribute("recentExperiments", recentExperimentsPage.getContent());

            // Add pagination information to the model
            model.addAttribute("currentPage", recentExperimentsPage.getNumber());
            model.addAttribute("totalPages", recentExperimentsPage.getTotalPages());

            return "home";
        } catch (BusinessException e) {
            // Handle business exception and return error view
            applicationLogger.severe(e.getErrorType() + " occurred: " + e.getMessage());
            model.addAttribute("error", "Internal server error");
            return "error";
        }
    }

    @GetMapping("/access-denied")
    public String accessDeniedPage() {
        return "access-denied";
    }




}

