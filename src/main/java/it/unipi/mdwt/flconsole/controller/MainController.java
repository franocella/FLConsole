package it.unipi.mdwt.flconsole.controller;

import it.unipi.mdwt.flconsole.service.UserService;
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

    private final UserService userService;
    private final ExpRunnerService expRunnerService;

    private void expConfigServiceSaveConfigSTUB(String userConfigurations) {
        // STUB implementation
    }
    private List<String> expConfigServiceGetUsersConfigListSTUB(String email) {
        // STUB implementation
        return null;
    }
    @Autowired
    public MainController(UserService userService, ExpRunnerService expRunnerService) {
        this.userService = userService;
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
            // TODO: Call the creation Config service
            // Simulating success
                // If the new configuration is successful, fetch user configurations
                List<String> userConfigurations = expConfigServiceGetUsersConfigListSTUB("email@asljh.com");
                model.addAttribute("configurations", userConfigurations);
            return "main";
        } catch (Exception e) {
            // If an exception occurs during the process, return a server error message
            model.addAttribute("error", "Internal server error");
            return "error";
        }
    }

    @GetMapping("/newConfig")
    public ResponseEntity<?> newConfig() {
    try {
        // TODO: Call the creation Config service
        // If the new configuration is successful, fetch user configurations
        expConfigServiceSaveConfigSTUB("config");
        return ResponseEntity.ok("Configuration saved");
    } catch (Exception e) {
        // If an exception occurs during the process, return a server error message
        // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        // If the new configuration fails, return an error message
        String errorMessage = "Error during new configuration";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }
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

