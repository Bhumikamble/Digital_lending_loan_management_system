package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.entity.Role;
import com.miniSprint.Digital_lending_loan_management_system.entity.User;
import com.miniSprint.Digital_lending_loan_management_system.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminRegistrationCode;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.registration-code:}") String adminRegistrationCode) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminRegistrationCode = adminRegistrationCode;
    }

    public User registerUser(
            String email,
            String password,
            String selectedRole,
            String adminCode) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        Role role;

        try {
            role = Role.valueOf(selectedRole);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new RuntimeException("Please select a valid role");
        }

        if (role == Role.ADMIN) {
            if (adminRegistrationCode.isBlank()
                    || adminCode == null
                    || !adminRegistrationCode.equals(adminCode)) {
                throw new RuntimeException("Invalid admin authorization code");
            }
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }
}