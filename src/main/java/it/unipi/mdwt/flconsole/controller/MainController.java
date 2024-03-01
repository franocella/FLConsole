package it.unipi.mdwt.flconsole.controller;

import it.unipi.mdwt.flconsole.service.AuthenticationService;
import it.unipi.mdwt.flconsole.service.ExpRunnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class MainController {

    private final AuthenticationService authenticationService;
    private final ExpRunnerService expRunnerService;

    @Autowired
    public MainController(AuthenticationService authenticationService, ExpRunnerService expRunnerService) {
        this.authenticationService = authenticationService;
        this.expRunnerService = expRunnerService;
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
            authenticationService.authenticate(email, password);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }

        // TODO: implement cookie creation with user email

        return "redirect:/home";
    }


    @GetMapping("/")
    public String home() {
        return "main";
    }

    @GetMapping("/newConfig")
    public ResponseEntity<?> newConfig() {
    /*
    try {
        // TODO: Call the creation Config service
        // Simulating success
        boolean success = true;
        if (success) {
            // If the new configuration is successful, fetch user configurations
            List<String> userConfigurations = expConfigService.getUsersConfigList();
            return ResponseEntity.ok(userConfigurations);
        } else {
            // If the new configuration fails, return an error message
            String errorMessage = "Error during new configuration";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    } catch (Exception e) {
        // If an exception occurs during the process, return a server error message
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }*/
        return null;
    }


    @GetMapping("testWebSocket")
    public String testWebSocket() {
        return "testWebSocket";
    }

    @PostMapping("/start-task")
    public ResponseEntity<?> startTask() {
        try {
            expRunnerService.runExp();
            return ResponseEntity.ok("Task started successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting the task");
        }
    }



}

