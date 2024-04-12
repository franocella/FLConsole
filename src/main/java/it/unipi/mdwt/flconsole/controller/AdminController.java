package it.unipi.mdwt.flconsole.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import it.unipi.mdwt.flconsole.dto.ExperimentSummary;
import it.unipi.mdwt.flconsole.model.*;
import it.unipi.mdwt.flconsole.service.*;
import it.unipi.mdwt.flconsole.utils.exceptions.business.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
            String email = cookieService.getCookieValue(request.getCookies(),"email");
            User user = userService.getUser(email);
            if (user.getConfigurations()!=null){
                Page<ExpConfig> userConfigurations = expConfigService.getNConfigsList(user.getConfigurations());
                int totalConfigPages = userConfigurations.getTotalPages();
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
                model.addAttribute("totalConfigPages", totalConfigPages);
            }
            if (user.getExperiments()!=null){
                int totalExpPages = (int) Math.ceil((double) user.getExperiments().size() / PAGE_SIZE);
                List<ExperimentSummary> experimentSummaries = user.getExperiments().stream()
                        .sorted(Comparator.comparing(ExperimentSummary::getCreationDate).reversed())
                        .limit(Math.min(user.getExperiments().size(), PAGE_SIZE))
                        .toList();
                model.addAttribute("experiments", experimentSummaries);
                model.addAttribute("totalExpPages", totalExpPages);
            }
            return "adminDashboard";

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
            applicationLogger.severe("parameters:" + config.getParameters());
            // Perform the configuration save
            expConfigService.saveConfig(config, email);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Create the JSON response with the data
            Map<String, Object> response = new HashMap<>();
            response.put("id", config.getId());

            if (config.getCreationDate() != null) {
                String creationTime = dateFormat.format(config.getCreationDate());
                response.put("creationTime", creationTime);
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

    @PostMapping("/newExp")
    public ResponseEntity<String> newExp(@RequestBody String exp, HttpServletRequest request) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Convert the JSON string to an ExpConfig object
            Experiment experiment = objectMapper.readValue(exp, Experiment.class);

            System.out.println(experiment);

            String email = cookieService.getCookieValue(request.getCookies(),"email");

            // Perform the configuration save
            experimentService.saveExperiment(experiment, email);

            // Create the JSON response with the data
            Map<String, Object> response = new HashMap<>();
            response.put("id", experiment.getId());
            response.put("name", experiment.getName());
            response.put("configName", experiment.getExpConfig().getName());
            response.put("algorithm", experiment.getExpConfig().getAlgorithm());
            if (experiment.getCreationDate() != null) {
                String creationTime = dateFormat.format(experiment.getCreationDate());
                response.put("creationTime", creationTime);
            }

            // Convert the response map to a JSON string
            String jsonResponse = objectMapper.writeValueAsString(response);
            System.out.println(jsonResponse);
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
        expConfigService.deleteExpConfig(id, email);

        String message = "Config with ID " + id + " successfully deleted.";
        return ResponseEntity.ok(message);
    }

    @GetMapping("/deleteExp-{id}")
    public ResponseEntity<String> deleteExperiment(@PathVariable String id, HttpServletRequest request) {

        String email = cookieService.getCookieValue(request.getCookies(),"email");
        experimentService.deleteExperiment(id, email);

        String message = "Experiment with ID " + id + " successfully deleted.";
        return ResponseEntity.ok(message);
    }

    @PostMapping("/start-exp")
    public ResponseEntity<?> startTask(HttpServletRequest request) {
        try {

            // Get the value of the 'config' parameter from the query string
            String config = request.getParameter("config");

            // Get the value of the 'expId' parameter from the query string
            String expId = request.getParameter("expId");

            // Check if the 'config' and 'expId' parameters are present and not blank
            if (StringUtils.isBlank(config) || StringUtils.isBlank(expId)) {
                return ResponseEntity.badRequest().body("Missing or blank required parameters");
            }

            // Run the experiment task
            experimentService.runExp(config, expId);

            // Return success response
            return ResponseEntity.ok("Task started successfully");
        } catch (Exception e) {
            // Return internal server error response if an exception occurs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting the task");
        }
    }

    @GetMapping("/getExperiments")
    public ResponseEntity<Page<ExperimentSummary>> searchExp(@RequestParam int page, String executionName, String configName, HttpServletRequest request) {
        String email = cookieService.getCookieValue(request.getCookies(),"email");
        Page<ExperimentSummary> experiments = experimentService.getMyExperiments(email, executionName, configName, page);
        return ResponseEntity.ok(experiments);
    }

    @GetMapping("/getConfigurations")
    public ResponseEntity<Page<ExpConfig>> searchConfig(@RequestParam int page, String name, String clientStrategy, String stopCondition, HttpServletRequest request) {
        String email = cookieService.getCookieValue(request.getCookies(),"email");
        Page<ExpConfig> expConfigs = expConfigService.searchMyExpConfigs(email, name, clientStrategy, stopCondition, page);
        return ResponseEntity.ok(expConfigs);
    }



}
