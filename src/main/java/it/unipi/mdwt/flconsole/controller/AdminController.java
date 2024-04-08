package it.unipi.mdwt.flconsole.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import it.unipi.mdwt.flconsole.model.*;
import it.unipi.mdwt.flconsole.service.*;
import it.unipi.mdwt.flconsole.utils.MessageType;
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
import java.util.stream.Collectors;

import static it.unipi.mdwt.flconsole.utils.Constants.PAGE_SIZE;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ExperimentService experimentService;
    private final ExpConfigService expConfigService;
    private final Logger applicationLogger;
    private final MetricsService metricsService;
    private final CookieService cookieService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AdminController(UserService userService, ExperimentService experimentService, ExpConfigService expConfigService, Logger applicationLogger, MetricsService metricsService, CookieService cookieService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.experimentService = experimentService;
        this.expConfigService = expConfigService;
        this.applicationLogger = applicationLogger;
        this.metricsService = metricsService;
        this.cookieService = cookieService;
        this.objectMapper = objectMapper;
    }



    @GetMapping("/dashboard")
    public String home(Model model, HttpServletRequest request) {
        try {
            String email = cookieService.getCookieValue(request.getCookies(),"email");
            User user = userService.getUser(email);
            if (user.getConfigurations()!=null){
                Page<ExpConfig> userConfigurations = expConfigService.getNconfigsList(user.getConfigurations());
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
                model.addAttribute("experiments", user.getExperiments().subList(0, Math.min(user.getExperiments().size(), PAGE_SIZE)));
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

    @GetMapping("/experiment-{id}")
    public String experimentDetails(@PathVariable String id, Model model, HttpServletRequest request) {

        Experiment experiment;
        ExpConfig expConfig;
        try {
            String role = cookieService.getCookieValue(request.getCookies(),"role");
            if (role != null && role.equals("admin")) {
                Boolean isAuthor = userService.isExperimentAuthor(cookieService.getCookieValue(request.getCookies(),"email"), id);
                model.addAttribute("isAuthor", isAuthor);
            }

            experiment = experimentService.getExpDetails(id);
            model.addAttribute("experiment", experiment);

            // TODO: Implement getExpConfigById (better)
            expConfig = expConfigService.getNconfigsList(List.of(experiment.getExpConfig().getId())).getContent().get(0);
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
                            Map<String, Object> tempMap = mapper.convertValue(expMetrics, new TypeReference<Map<String, Object>>() {});
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


            System.out.println("Starting task with config: " + config + " and expId: " + expId);
            // Get the email from the cookie
            String email = cookieService.getCookieValue(request.getCookies(), "email");

            // Run the experiment task
            experimentService.runExp(config, email, expId);
            System.out.println("Task started successfully");
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
        Page<ExperimentSummary> experiments = experimentService.searchMyExperiments(email, executionName, configName, page);
        return ResponseEntity.ok(experiments);
    }

    @GetMapping("/getConfigurations")
    public ResponseEntity<Page<ExpConfig>> searchConfig(@RequestParam int page, String name, String clientStrategy, String stopCondition, HttpServletRequest request) {
        String email = cookieService.getCookieValue(request.getCookies(),"email");
        Page<ExpConfig> expConfigs = expConfigService.searchMyExpConfigs(email, name, clientStrategy, stopCondition, page);
        return ResponseEntity.ok(expConfigs);
    }



}
