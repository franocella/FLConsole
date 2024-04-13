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
import it.unipi.mdwt.flconsole.utils.MessageType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
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
    public String home(Model model) {
        Page<Experiment> experiments = experimentService.getExperiments(null, null, 0);
        model.addAttribute("experiments", experiments);
        return "userDashboard";
    }

    @PostMapping("/getExperiments")
    public ResponseEntity<Page<Experiment>> searchAllExp (@RequestParam int page, String expName, String configName) {
        try {
            applicationLogger.severe("Searching experiments with name: " + expName + " and configName: " + configName);
            Page<Experiment> experiments = experimentService.getExperiments(expName, configName, page);
            applicationLogger.severe("Experiment number pages: " + experiments.getTotalPages());
            return ResponseEntity.ok(experiments);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @GetMapping("/experiment-{id}")
    public String experimentDetails(@PathVariable String id, Model model, HttpServletRequest request) {

        Experiment experiment;
        ExpConfig expConfig;
        try {
            String role = cookieService.getCookieValue(request.getCookies(),"role");
            if (role != null && role.equals("admin")) {
                Boolean isAuthor = userService.isExperimentAuthor(cookieService.getCookieValue(request.getCookies(),"email"), id);
                model.addAttribute("isAuthor", isAuthor);
            } else {
                model.addAttribute("isAuthor", false);
            }

            experiment = experimentService.getExpDetails(id);
            model.addAttribute("experiment", experiment);

            // TODO: Implement getExpConfigById (better)
            expConfig = expConfigService.getNConfigsList(List.of(experiment.getExpConfig().getId()), null).getContent().get(0);
            applicationLogger.severe("expConfig: " + expConfig);
            model.addAttribute("expConfig", expConfig);

            // Retrieve the list of ExpMetrics for the given experiment ID
            List<ExpMetrics> expMetricsList = metricsService.getMetrics(experiment.getId());

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

            String jsonArray = "[" + String.join(",", jsonList) + "]";

            model.addAttribute("metrics", jsonArray);

            return "experimentDetails";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching experiment details");
            return "error";
        }

    }
    @GetMapping("/access-denied")
    public String accessDeniedPage() {
        return "access-denied";
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

