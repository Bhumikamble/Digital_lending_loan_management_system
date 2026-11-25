
package com.miniSprint.Digital_lending_loan_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain 8 characters, uppercase, lowercase, number and special character"
    )
    private String password;

    @NotBlank(message = "Please select a role")
    @Pattern(
            regexp = "BORROWER|ADMIN",
            message = "Please select a valid role"
    )
    private String role;

    private String adminCode;
}
