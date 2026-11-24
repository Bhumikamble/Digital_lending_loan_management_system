
package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.entity.Role;
import com.miniSprint.Digital_lending_loan_management_system.entity.User;
import com.miniSprint.Digital_lending_loan_management_system.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String email, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        // Public registration always creates a BORROWER
        user.setRole(Role.BORROWER);

        return userRepository.save(user);
    }
}
