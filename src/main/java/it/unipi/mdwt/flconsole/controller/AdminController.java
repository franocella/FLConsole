package it.unipi.mdwt.flconsole.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import it.unipi.mdwt.flconsole.dto.ExpConfigSummary;
import it.unipi.mdwt.flconsole.dto.ExperimentSummary;
import it.unipi.mdwt.flconsole.model.*;
import it.unipi.mdwt.flconsole.service.*;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static it.unipi.mdwt.flconsole.utils.Constants.PAGE_SIZE;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ExperimentService experimentService;
    private final ExpConfigService expConfigService;
    private final Logger applicationLogger;
    private final CookieService cookieService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AdminController(UserService userService, ExperimentService experimentService, ExpConfigService expConfigService, Logger applicationLogger, CookieService cookieService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.experimentService = experimentService;
        this.expConfigService = expConfigService;
        this.applicationLogger = applicationLogger;
        this.cookieService = cookieService;
        this.objectMapper = objectMapper;
    }



    @GetMapping("/dashboard")
    public String home(Model model, HttpServletRequest request) {
        try {
            // Retrieve the email from the cookie
            String email = cookieService.getCookieValue(request.getCookies(),"email");
            // Retrieve the user object based on the email
            User user = userService.getUser(email);

            if (user.getConfigurations() != null){

                Page<ExpConfig> allConfigurations = expConfigService.getConfigsListFirstPage(user.getConfigurations(), user.getConfigurations().size());
                List<ExpConfigSummary> allConfigurationsSummary = allConfigurations.getContent().stream()
                        .map(ExpConfig::toSummary)
                        .toList();
                model.addAttribute("allConfigurations", allConfigurationsSummary);

                // Create a Page object with the first PAGE_SIZE configurations
                Page<ExpConfig> userConfigurations = new PageImpl<>(
                        allConfigurations.getContent().subList(0, Math.min(PAGE_SIZE, allConfigurations.getContent().size())),
                        PageRequest.of(0, PAGE_SIZE),
                        allConfigurations.getTotalElements());
                model.addAttribute("configurations", userConfigurations);

                // Add formatted creation dates to the model
                Map<String, String> ConfigDate = new HashMap<>();
                userConfigurations.getContent().forEach(config ->
                        ConfigDate.put(config.getId(), new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(config.getCreationDate())));
                model.addAttribute("configsDate", ConfigDate);
            }

            if (user.getExperiments() != null) {
                Map<String, String> ExperimentsDate = new HashMap<>();
                List<ExperimentSummary> experimentSummaries = user.getExperiments().stream()
                        .sorted(Comparator.comparing(ExperimentSummary::getCreationDate).reversed())
                        .limit(Math.min(user.getExperiments().size(), PAGE_SIZE))
                        .peek(exp -> ExperimentsDate.put(exp.getId(), new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(exp.getCreationDate())))
                        .toList();

                Page<ExperimentSummary> userExperiments = new PageImpl<>(experimentSummaries, PageRequest.of(0, PAGE_SIZE), user.getExperiments().size());
                model.addAttribute("experiments", userExperiments);

                // Add formatted creation dates to the model
                model.addAttribute("experimentsDate", ExperimentsDate);
            }

            return "adminDashboard";
        } catch (BusinessException e) {
            // If an exception occurs during the process, return a server error message
            applicationLogger.severe(e.getErrorType() + " occurred: " + e.getMessage());
            model.addAttribute("error", "Internal server error");
            return "error";
        }
    }

    /**
     * Controller method to handle requests for creating a new configuration.
     *
     * @param expConfig The JSON string representing the configuration.
     * @param request   The HTTP servlet request.
     * @return A ResponseEntity containing a JSON response.
     */
    @PostMapping("/newConfig")
    public ResponseEntity<String> newConfig(@RequestBody String expConfig, HttpServletRequest request) {
        try {
            // Convert the JSON string to an ExpConfig object
            ExpConfig config = objectMapper.readValue(expConfig, ExpConfig.class);

            // Retrieve the email from the cookie
            String email = cookieService.getCookieValue(request.getCookies(),"email");

            // Perform the configuration save
            expConfigService.saveConfig(config, email);

            return ResponseEntity.ok(config.getId());
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


    /**
     * Controller method to handle requests for creating a new experiment.
     *
     * @param exp     The JSON string representing the experiment.
     * @param request The HTTP servlet request.
     * @return A ResponseEntity containing a JSON response.
     */
    @PostMapping("/newExp")
    public ResponseEntity<String> newExp(@RequestBody String exp, HttpServletRequest request) {
        try {
            // Convert the JSON string to an Experiment object
            Experiment experiment = objectMapper.readValue(exp, Experiment.class);

            // Retrieve the email from the cookie
            String email = cookieService.getCookieValue(request.getCookies(),"email");

            // Perform the experiment save
            experimentService.saveExperiment(experiment, email);

            return ResponseEntity.ok(experiment.getId());
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

    /**
     * Handles the deletion of a configuration by ID.
     *
     * @param id      The ID of the configuration to delete.
     * @param request The HTTP servlet request.
     * @return A ResponseEntity containing a message indicating the success or failure of the deletion operation.
     */
    @PostMapping("/deleteConfig-{id}")
    public ResponseEntity<String> deleteConfig(@PathVariable String id, HttpServletRequest request) {
        try {
            // Get the email of the user from the cookie
            String email = cookieService.getCookieValue(request.getCookies(),"email");
            // Delete the configuration using the service
            expConfigService.deleteExpConfig(id, email);
            // Construct a success message
            String message = "Config with ID " + id + " successfully deleted.";
            // Return a ResponseEntity with the success message
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            // If an exception occurs during deletion, log the error and return an error message
            applicationLogger.severe("Error deleting config with ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete config with ID " + id);
        }
    }

    /**
     * Handles the deletion of an experiment by ID.
     *
     * @param id      The ID of the experiment to delete.
     * @param request The HTTP servlet request.
     * @return A ResponseEntity containing a message indicating the success or failure of the deletion operation.
     */
    @PostMapping("/deleteExp-{id}")
    public ResponseEntity<String> deleteExperiment(@PathVariable String id, HttpServletRequest request) {
        try {
            // Get the email of the user from the cookie
            String email = cookieService.getCookieValue(request.getCookies(),"email");
            // Delete the experiment using the service
            experimentService.deleteExperiment(id, email);
            // Construct a success message
            String message = "Experiment with ID " + id + " successfully deleted.";
            // Return a ResponseEntity with the success message
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            // If an exception occurs during deletion, log the error and return an error message
            applicationLogger.severe("Error deleting experiment with ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete experiment with ID " + id);
        }
    }


    /**
     * Handles the request to start an experiment task.
     * This method expects 'config' and 'expId' parameters in the query string.
     *
     * @param request The HTTP servlet request containing the query parameters.
     * @return A ResponseEntity containing a message indicating the success or failure of the task start operation.
     */
    @PostMapping("/start-exp")
    public ResponseEntity<String> startExp(HttpServletRequest request) {
        try {

            // Get the value of the 'config' parameter from the query string
            String config = request.getParameter("config");

            // Get the value of the 'expId' parameter from the query string
            String expId = request.getParameter("expId");

            // Check if the 'config' and 'expId' parameters are present and not blank
            if (StringUtils.isBlank(config) || StringUtils.isBlank(expId)) {
                // Return a bad request response if required parameters are missing or blank
                return ResponseEntity.badRequest().body("Missing or blank required parameters");
            }

            // Run the experiment task using the provided configuration and experiment ID
            experimentService.runExp(config, expId);

            // Return success response
            return ResponseEntity.ok("Task started successfully");
        } catch (Exception e) {
            // If an exception occurs during task start, return internal server error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting the task");
        }
    }

    /**
     * Retrieves experiments based on specified search criteria.
     *
     * @param page          The page number to retrieve.
     * @param executionName The name of the execution to search for (optional).
     * @param configName    The name of the configuration to search for (optional).
     * @param request       The HTTP servlet request containing user information.
     * @return A ResponseEntity containing a Page of ExperimentSummary objects matching the search criteria.
     */
    @GetMapping("/getExperiments")
    public ResponseEntity<Page<ExperimentSummary>> searchExp(@RequestParam int page, String executionName, String configName, HttpServletRequest request) {
        try {
            // Get the email of the user from the cookie
            String email = cookieService.getCookieValue(request.getCookies(), "email");

            // Retrieve experiments based on the specified search criteria
            Page<ExperimentSummary> experiments = experimentService.getMyExperiments(email, executionName, configName, page);
            applicationLogger.severe("number of pages: " + experiments.getTotalPages());

            // Return the experiments as a ResponseEntity with OK status
            return ResponseEntity.ok(experiments);
        } catch (Exception e) {
            // If an exception occurs, log the error and return an internal server error response
            applicationLogger.severe("Error retrieving experiments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Retrieves configurations based on specified search criteria.
     *
     * @param page           The page number to retrieve.
     * @param name           The name of the configuration to search for (optional).
     * @param clientStrategy The client strategy of the configuration to search for (optional).
     * @param stopCondition  The stop condition of the configuration to search for (optional).
     * @param algorithm      The algorithm of the configuration to search for (optional).
     * @param request        The HTTP servlet request containing user information.
     * @return A ResponseEntity containing a Page of ExpConfig objects matching the search criteria.
     */
    @GetMapping("/getConfigurations")
    public ResponseEntity<Page<ExpConfig>> searchConfig(@RequestParam int page, String name, String clientStrategy, String stopCondition, String algorithm, HttpServletRequest request) {
        try {
            // Get the email of the user from the cookie
            String email = cookieService.getCookieValue(request.getCookies(), "email");

            // Retrieve configurations based on the specified search criteria
            Page<ExpConfig> expConfigs = expConfigService.searchMyExpConfigs(email, name, clientStrategy, stopCondition, algorithm, page);

            // Return the configurations as a ResponseEntity with OK status
            return ResponseEntity.ok(expConfigs);
        } catch (Exception e) {
            // If an exception occurs, log the error and return an internal server error response
            applicationLogger.severe("Error retrieving configurations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getConfigDetails")
    public ResponseEntity<ExpConfig> getConfigDetails(@RequestParam String id) {
        try {
            // Retrieve the configuration details
            ExpConfig expConfig = expConfigService.getExpConfigById(id);

            // Return the configuration as a ResponseEntity with OK status
            return ResponseEntity.ok(expConfig);
        } catch (Exception e) {
            // If an exception occurs, log the error and return an internal server error response
            applicationLogger.severe("Error retrieving configuration details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
