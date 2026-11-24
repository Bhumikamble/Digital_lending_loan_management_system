
package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.dto.RegisterRequest;
import com.miniSprint.Digital_lending_loan_management_system.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Display registration page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    // Handle registration form submission
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            authService.registerUser(
                    request.getEmail(),
                    request.getPassword()
            );

            return "redirect:/login?registered";

        } catch (RuntimeException exception) {
            model.addAttribute("error", exception.getMessage());
            return "register";
        }
    }

    // Display login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}

