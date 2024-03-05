package it.unipi.mdwt.flconsole.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import it.unipi.mdwt.flconsole.model.Experiment;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class MainController {

    private final UserService userService;
    private final ExperimentService experimentService;
    private final ExpConfigService expConfigService;
    private final Logger applicationLogger;

    private void expConfigServiceSaveConfigSTUB(String userConfigurations) {
        // STUB implementation
    }
    private List<String> expConfigServiceGetUsersConfigListSTUB(String email) {
        // STUB implementation
        return null;
    }
    @Autowired
    public MainController(UserService userService, ExperimentService experimentService, ExpConfigService expConfigService, Logger applicationLogger) {
        this.userService = userService;
        this.experimentService = experimentService;
        this.expConfigService = expConfigService;
        this.applicationLogger = applicationLogger;
    }

    @GetMapping("/login")
    public String loginGET() {
        return "login";
    }

    @PostMapping("/login")
    public String loginPOST(HttpServletRequest request, HttpServletResponse response, Model model) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            userService.authenticate(email, password);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }

        // TODO: implement cookie creation with user email

        return "redirect:/home";
    }


    @GetMapping("/")
    public String home(Model model) {
        try {
            List<ExpConfig> userConfigurations = experimentService.getExpConfigList("firstTest@example.com");
            model.addAttribute("configurations", userConfigurations);
            return "main";
        } catch (BusinessException e) {
            // If an exception occurs during the process, return a server error message
            applicationLogger.severe(e.getErrorType()+" occurred:" + e.getMessage());
            model.addAttribute("error", "Internal server error");
            return "error";
        }
    }





    @PostMapping("/newConfig")
    public ResponseEntity<String> newConfig(@RequestBody String expConfig) {
        try {
            // Convert the JSON string to an ExpConfig object
            ObjectMapper objectMapper = new ObjectMapper();
            ExpConfig config = objectMapper.readValue(expConfig, ExpConfig.class);

            //TODO - Implement the user email retrieval
            String email = "firstTest@example.com";

            // Perform the configuration save
            expConfigService.saveConfig(config, email);

            // Create the JSON response with the data
            Map<String, Object> response = new HashMap<>();
            response.put("id", config.getId());
            response.put("creationTime", config.getCreationDate());
            response.put("lastUpdate", config.getLastUpdate());

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

