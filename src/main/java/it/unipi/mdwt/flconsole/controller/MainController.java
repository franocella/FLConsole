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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.naming.AuthenticationException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public ResponseEntity<String> loginPOSTJson(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            userService.authenticate(email, password);

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
    public String home(Model model, HttpServletRequest request) {
        try {
            String email = cookieService.getCookieValue(request.getCookies(),"email");
            List<ExpConfig> userConfigurations = expConfigService.getExpConfigsForUser(email);

            List<String> jsonList = userConfigurations.stream()
                    .filter(Objects::nonNull) // Filter out null values
                    .map(expConfig -> {
                        try {
                            return objectMapper.writeValueAsString(expConfig);
                        } catch (JsonProcessingException e) {
                            // Handle the exception if the conversion fails
                            applicationLogger.severe("Error converting ExpConfig to JSON: " + e.getMessage());
                            return null;
                        }
                    })
                    .toList();

            model.addAttribute("configurations", jsonList);
            return "main";
        } catch (BusinessException e) {
            // If an exception occurs during the process, return a server error message
            applicationLogger.severe(e.getErrorType()+" occurred:" + e.getMessage());
            model.addAttribute("error", "Internal server error");
            return "error";
        }
    }





    @PostMapping("/newConfig")
    public ResponseEntity<String> newConfig(@RequestBody String expConfig, HttpServletRequest request) {
        try {
            // Convert the JSON string to an ExpConfig object
            ExpConfig config = objectMapper.readValue(expConfig, ExpConfig.class);

            String email = cookieService.getCookieValue(request.getCookies(),"email");

            // Perform the configuration save
            expConfigService.saveConfig(config, email);

            System.out.println(config.getId()+" "+config.getCreationDate()+" "+config.getLastUpdate());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Create the JSON response with the data
            Map<String, Object> response = new HashMap<>();
            response.put("id", config.getId());

            if (config.getCreationDate() != null) {
                String creationTime = dateFormat.format(config.getCreationDate());
                response.put("creationTime", creationTime);
            }

            if (config.getLastUpdate() != null) {
                String lastUpdate = dateFormat.format(config.getLastUpdate());
                response.put("lastUpdate", lastUpdate);
            }

            // Convert the response map to a JSON string
            String jsonResponse = objectMapper.writeValueAsString(response);

            // Return the JSON response
            return ResponseEntity.ok(jsonResponse);

        } catch (JsonProcessingException e) {
            // Handle the error in JSON string parsing
            applicationLogger.severe("Error parsing JSON: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format");
        } catch (BusinessException e) {
            // Handle the business exception
            applicationLogger.severe(e.getErrorType() + " occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/deleteConfig-{id}")
    public ResponseEntity<String> deleteConfig(@PathVariable String id, HttpServletRequest request) {


        String email = cookieService.getCookieValue(request.getCookies(),"email");
        expConfigService.deleteConfig(id, email);

        String message = "Config with ID " + id + " successfully deleted.";
        return ResponseEntity.ok(message);
    }



    @GetMapping("experiment-{id}")
    public String experimentDetails(@PathVariable String id, Model model) {

        Experiment experiment;
        try {
            experiment = experimentService.getExpDetails(id);
            model.addAttribute("experiment", experiment);
            return "experimentDetails";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching experiment details");
            return "error";
        }

    }

    @PostMapping("/start-task")
    public ResponseEntity<?> startTask() {
        try {
            experimentService.runExp();
            return ResponseEntity.ok("Task started successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting the task");
        }
    }

}

