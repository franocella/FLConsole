package it.unipi.mdwt.flconsole.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.dto.UserDTO;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.ExperimentSummary;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.service.CookieService;
import it.unipi.mdwt.flconsole.service.ExpConfigService;
import it.unipi.mdwt.flconsole.service.UserService;
import it.unipi.mdwt.flconsole.service.ExperimentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.naming.AuthenticationException;
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
        cookieService.deleteCookie("role", response);
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
    public String home(Model model, HttpServletRequest request) {
        List<Pair<ExperimentSummary, String>> experiments = experimentService.getExperimentsSummaryList(10);
        model.addAttribute("experiments", experiments);
        return "userDashboard";
    }

    /*@GetMapping("/")
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
    }*/

    @GetMapping("/access-denied")
    public String accessDeniedPage() {
        return "access-denied";
    }

    @GetMapping("/experiment-{id}")
    public String experimentDetails(@PathVariable String id, Model model, HttpServletRequest request) {

        Experiment experiment;
        String role = cookieService.getCookieValue(request.getCookies(),"role");
        if (role != null && role.equals("admin")) {
            model.addAttribute("role", "admin");
        } else {
            model.addAttribute("role", "user");
        }
        try {
            experiment = experimentService.getExpDetails(id);
            model.addAttribute("experiment", experiment);
            return "experimentDetails";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching experiment details");
            return "error";
        }

    }
    @GetMapping("profile")
    public String profile(Model model, HttpServletRequest request) {
        String email = cookieService.getCookieValue(request.getCookies(),"email");
        User user = userService.getUser(email);
        model.addAttribute("user", user);
        return "profilePage";
    }

    @PostMapping("/profile/update")
    public ResponseEntity<String> updateProfile(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get the new email, password, and description from the request parameters
            String newEmail = request.getParameter("email");
            String newPassword = request.getParameter("password");
            String newDescription = request.getParameter("description");

            // Print the received data for debugging
            System.out.println("Email: " + newEmail);
            System.out.println("Password: " + newPassword);
            System.out.println("Description: " + newDescription);

            // Get the email from the cookie
            String email = cookieService.getCookieValue(request.getCookies(), "email");
            System.out.println("Email retrieved from cookie: " + email);

            // Check if all three parameters are null
            if (newEmail == null && newPassword == null && newDescription == null) {
                // Return bad request response if all parameters are null
                return ResponseEntity.badRequest().body("At least one parameter (email, password, description) must be provided for update.");
            }

            // Create a new UserDTO object with the updated fields
            UserDTO updateUser = new UserDTO(newEmail, newPassword, newDescription);
            // Update the user profile
            userService.updateUserProfile(email, updateUser);

            // Set the email cookie with the new value
            if (newEmail != null) {
                cookieService.setCookie("email", newEmail, response);
            }

            // Return a success response
            return ResponseEntity.ok().body("Profile update successful!");
        } catch (Exception e) {
            // Log the exception
            applicationLogger.severe("Error occurred while updating profile: " + e.getMessage());

            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the profile.");
        }
    }






    @GetMapping("/profile/delete")
    public ResponseEntity<String> deleteUser(HttpServletRequest request, HttpServletResponse response) {
        // Delete the user details calling the service
        // Return the JSON response
        try {
            String email = cookieService.getCookieValue(request.getCookies(),"email");
            userService.deleteAccount(email);
            cookieService.deleteCookie("email", response);
            cookieService.deleteCookie("role", response);
            // Return success response
            return ResponseEntity.ok().body("Profile deleted successful!");
        } catch (Exception e) {
            // Log exception
            applicationLogger.severe("Error occurred while deleting profile: " + e.getMessage());

            // Return error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the profile.");
        }
    }
}

