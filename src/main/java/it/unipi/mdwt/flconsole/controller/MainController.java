package it.unipi.mdwt.flconsole.controller;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.dto.UserSummary;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.ExpMetrics;
import it.unipi.mdwt.flconsole.model.Experiment;
import it.unipi.mdwt.flconsole.model.User;
import it.unipi.mdwt.flconsole.service.*;
import it.unipi.mdwt.flconsole.utils.ExperimentStatus;
import it.unipi.mdwt.flconsole.utils.MessageType;
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
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Controller
@RequestMapping("/")
public class MainController {

    private final UserService userService;
    private final ExperimentService experimentService;
    private final ExpConfigService expConfigService;
    private final Logger applicationLogger;
    private final CookieService cookieService;
    private final ObjectMapper objectMapper;

    private final MetricsService metricsService;

    @Autowired
    public MainController(UserService userService, ExperimentService experimentService, ExpConfigService expConfigService, Logger applicationLogger, CookieService cookieService, ObjectMapper objectMapper, MetricsService metricsService) {
        this.userService = userService;
        this.experimentService = experimentService;
        this.expConfigService = expConfigService;
        this.applicationLogger = applicationLogger;
        this.cookieService = cookieService;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;
    }
    /**
     * Handles the GET request for the login page.
     *
     * @return The name of the login view page.
     */
    @GetMapping("/login")
    public String loginGET() {
        return "login";
    }
    /**
     * Handles the POST request for user authentication.
     *
     * @param request  The HTTP servlet request containing user credentials.
     * @param response The HTTP servlet response used to set cookies.
     * @return A ResponseEntity indicating the success or failure of the authentication attempt.
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginPOST(HttpServletRequest request, HttpServletResponse response) {
        // Retrieve user credentials from the request parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            // Attempt to authenticate the user
            String role = userService.authenticate(email, password);

            // Authentication successful, set cookies
            cookieService.setCookie("email", email, response);
            if (role != null) {
                cookieService.setCookie("role", role, response);
            }

            // Return a success JSON response
            return ResponseEntity.ok("{\"status\": \"success\"}");
        } catch (AuthenticationException | BusinessException e) {
            // If authentication fails, return an error JSON response with UNAUTHORIZED status
            applicationLogger.severe("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Handles the GET request to log out a user.
     *
     * @param response The HTTP servlet response used to delete cookies.
     * @return A redirect to the login page.
     */
    @GetMapping("/logout")
    public String logoutGET(HttpServletResponse response) {
        // Delete cookies related to user authentication
        cookieService.deleteCookie("email", response);
        cookieService.deleteCookie("role", response);

        // Redirect the user to the login page
        return "redirect:/FLConsole/login";
    }

    /**
     * Handles the GET request for the sign-up page.
     *
     * @return The name of the sign-up view page.
     */
    @GetMapping("/signup")
    public String signUpGET() {
        return "signup";
    }
    /**
     * Handles the POST request for user registration.
     *
     * @param request  The HTTP servlet request containing user registration data.
     * @param response The HTTP servlet response used to set cookies.
     * @return A ResponseEntity indicating the success or failure of the registration attempt.
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signUpPOST(HttpServletRequest request, HttpServletResponse response) {
        // Retrieve user registration data from the request parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            // Attempt to sign up the user
            userService.signUp(email, password);

            // Authentication successful, set cookie
            cookieService.setCookie("email", email, response);

            // Return a success JSON response
            return ResponseEntity.ok("{\"status\": \"success\"}");
        } catch (AuthenticationException e) {
            // If registration fails, return an error JSON response with UNAUTHORIZED status
            applicationLogger.severe("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }


    /**
     * Handles the GET request for the home page.
     *
     * @param model The model to be populated with data for the view.
     * @return The name of the user dashboard view page.
     */
    @GetMapping("/")
    public String homeGET(Model model) {
        // Retrieve experiments data for the user dashboard
        Page<Experiment> experiments = experimentService.getExperiments(null, null, 0);
        // Add experiments data to the model
        model.addAttribute("experiments", experiments);

        // Add experiment formatted creation date to the model
        Map<String, String> experimentsDate = new HashMap<>();
        experiments.getContent().forEach(exp ->
                experimentsDate.put(exp.getId(), new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(exp.getCreationDate())));

        model.addAttribute("experimentsDate", experimentsDate);

        // Return the name of the user dashboard view page
        return "userDashboard";
    }

    /**
     * Handles the POST request to search for experiments based on specified criteria.
     *
     * @param page       The page number to retrieve.
     * @param expName    The name of the experiment to search for (optional).
     * @param configName The name of the configuration associated with the experiment to search for (optional).
     * @return A ResponseEntity containing a Page of Experiment objects matching the search criteria.
     */
    @GetMapping("/getExperiments")
    public ResponseEntity<Page<Experiment>> searchAllExpGET(@RequestParam int page, String expName, String configName) {
        try {
            // Log the search criteria
            applicationLogger.severe("Searching experiments with name: " + expName + " and configName: " + configName);

            // Retrieve experiments based on the specified criteria
            Page<Experiment> experiments = experimentService.getExperiments(expName, configName, page);

            // Return the experiments as a ResponseEntity with OK status
            return ResponseEntity.ok(experiments);
        } catch (Exception e) {
            // If an exception occurs, return an internal server error response
            applicationLogger.severe("Error occurred while searching experiments: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles the GET request for viewing details of a specific experiment.
     *
     * @param id     The ID of the experiment to view details for.
     * @param model  The model to be populated with data for the view.
     * @param request The HTTP servlet request containing user information.
     * @return The name of the experiment details view page.
     */
    @GetMapping("/experiment-{id}")
    public String experimentDetailsGET(@PathVariable String id, Model model, HttpServletRequest request) {
        try {
            // Retrieve user role from cookies
            String role = cookieService.getCookieValue(request.getCookies(), "role");

            // Check if the user is an admin and if is the author of the experiment
            if (role != null && role.equals("admin")) {
                Boolean isAuthor = userService.isExperimentAuthor(cookieService.getCookieValue(request.getCookies(), "email"), id);
                model.addAttribute("isAuthor", isAuthor);
            } else {
                model.addAttribute("isAuthor", false);
            }

            // Retrieve details of the experiment
            Experiment experiment = experimentService.getExpDetails(id);
            applicationLogger.severe("Experiment: " + experiment);
            if (experiment == null) {
                return "error";
            }
            model.addAttribute("experiment", experiment);

            // Add experiment formatted creation date to the model
            String experimentDate = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(experiment.getCreationDate());
            model.addAttribute("experimentDate", experimentDate);

            applicationLogger.severe("Configuration: " + experiment.getExpConfig().getId());

            // Retrieve details of the experiment configuration
            ExpConfig expConfig = expConfigService.getExpConfigById(experiment.getExpConfig().getId());
            Optional<ExpConfig> optionalExpConfig = Optional.ofNullable(expConfig);
            if (optionalExpConfig.isPresent()) {
                expConfig = optionalExpConfig.get();
            }

            model.addAttribute("expConfig", Optional.ofNullable(expConfig));

            if (experiment.getStatus() != ExperimentStatus.NOT_STARTED) {
                // Retrieve the list of ExpMetrics for the given experiment ID
                List<ExpMetrics> expMetricsList = metricsService.getMetrics(experiment.getId());

                // Process and filter ExpMetrics data for visualization
                List<String> jsonList = expMetricsList.stream()
                        .filter(expMetrics -> expMetrics.getType() != null && expMetrics.getType().equals(MessageType.STRATEGY_SERVER_METRICS))
                        .map(expMetrics -> {
                            try {
                                // Configure ObjectMapper to exclude null fields
                                ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

                                // Create a temporary map to remove the expId field
                                Map<String, Object> tempMap = mapper.convertValue(expMetrics, new TypeReference<>() {});
                                tempMap.remove("expId");
                                tempMap.remove("type");

                                // Convert the map to JSON string
                                return mapper.writeValueAsString(tempMap);
                            } catch (JsonProcessingException e) {
                                applicationLogger.severe("Error converting ExpMetrics to JSON: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // Convert the list of filtered ExpMetrics JSON strings to a single JSON array
                String jsonArray = "[" + String.join(",", jsonList) + "]";

                // Add the JSON array of metrics data to the model
                model.addAttribute("metrics", jsonArray);
            }

            // Return the name of the experiment details view page
            return "experimentDetails";
        } catch (Exception e) {
            // If an error occurs during fetching of experiment details, display error page
            model.addAttribute("error", "Error fetching experiment details");
            return "error";
        }
    }

    /**
     * Handles the GET request for the access denied page.
     *
     * @return The name of the access denied view page.
     */
    @GetMapping("/access-denied")
    public String accessDeniedPageGET() {
        return "access-denied";
    }


    /**
     * Handles the GET request for the error page.
     *
     * @return The name of the error view page.
     */
    @GetMapping("/error")
    public String errorPageGET() {
        return "error";
    }

    /**
     * Handles the GET request for the user profile page.
     *
     * @param model   The model to be populated with user profile data.
     * @param request The HTTP servlet request containing user information.
     * @return The name of the user profile view page.
     */
    @GetMapping("/profile")
    public String profileGET(Model model, HttpServletRequest request) {
        // Retrieve user email from cookies
        String email = cookieService.getCookieValue(request.getCookies(), "email");

        // Retrieve user information from the database
        User user = userService.getUser(email);

        // Add user information to the model
        model.addAttribute("user", user);

        // Return the name of the user profile view page
        return "profilePage";
    }

    /**
     * Handles the POST request to update the user profile.
     *
     * @param request  The HTTP servlet request containing updated user profile data.
     * @param response The HTTP servlet response used to set cookies.
     * @return A ResponseEntity indicating the success or failure of the profile update.
     */
    @PostMapping("/profile/update")
    public ResponseEntity<String> updateProfilePOST(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get the new email, password, and description from the request parameters
            String newEmail = request.getParameter("email");
            String newPassword = request.getParameter("password");
            String newDescription = request.getParameter("description");

            // Get the email from the cookie
            String email = cookieService.getCookieValue(request.getCookies(), "email");

            // Check if at least one parameter is provided for update
            if (newEmail == null && newPassword == null && newDescription == null) {
                // Return bad request response if all parameters are null
                return ResponseEntity.badRequest().body("At least one parameter (email, password, description) must be provided for update.");
            }

            // Create a new UserDTO object with the updated fields
            UserSummary updateUser = new UserSummary(newEmail, newPassword, newDescription);

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

    /**
     * Handles the GET request to delete the user profile.
     *
     * @param request  The HTTP servlet request containing user information.
     * @param response The HTTP servlet response used to delete cookies.
     * @return A ResponseEntity indicating the success or failure of the profile deletion.
     */
    @PostMapping("/profile/delete")
    public ResponseEntity<String> deleteUserGET(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Retrieve user email from cookies
            String email = cookieService.getCookieValue(request.getCookies(), "email");

            // Delete the user account
            userService.deleteAccount(email);

            // Delete email and role cookies
            cookieService.deleteCookie("email", response);
            cookieService.deleteCookie("role", response);

            // Return success response
            return ResponseEntity.ok().body("Profile deleted successfully!");
        } catch (Exception e) {
            // Log exception
            applicationLogger.severe("Error occurred while deleting profile: " + e.getMessage());

            // Return error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the profile.");
        }
    }

    @GetMapping("/error")
    public String errorPageGET(Model model, HttpServletRequest request) {
        String role = cookieService.getCookieValue(request.getCookies(), "role");
        applicationLogger.severe("Role:" + role);
        if (role != null && role.equals("admin")) {
            model.addAttribute("isAdmin", true);
        } else {
            model.addAttribute("isAdmin", false);
        }
        return "error";
    }
}

