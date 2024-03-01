package it.unipi.mdwt.flconsole.controller;

import it.unipi.mdwt.flconsole.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainController {

    private final AuthenticationService authenticationService;

    @Autowired
    public MainController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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

        //TODO: implement cookie creation with user email

        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {
        return "main";
    }
}
