package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.entity.Role;
import com.miniSprint.Digital_lending_loan_management_system.entity.User;
import com.miniSprint.Digital_lending_loan_management_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);

        // Last value is the valid admin registration code for this test.
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                "ADMIN123"
        );
    }

    @Test
    void registersBorrowerSuccessfully() {
        when(userRepository.existsByEmail("borrower@test.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.registerUser(
                "borrower@test.com",
                "Password@123",
                "BORROWER",
                null
        );

        assertEquals("borrower@test.com", user.getEmail());
        assertEquals("encoded-password", user.getPassword());
        assertEquals(Role.BORROWER, user.getRole());
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("borrower@test.com"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.registerUser(
                        "borrower@test.com",
                        "Password@123",
                        "BORROWER",
                        null
                )
        );

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void rejectsAdminWithWrongRegistrationCode() {
        when(userRepository.existsByEmail("admin@test.com"))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.registerUser(
                        "admin@test.com",
                        "Password@123",
                        "ADMIN",
                        "WRONG-CODE"
                )
        );

        assertEquals(
                "Invalid admin authorization code",
                exception.getMessage()
        );
    }
}