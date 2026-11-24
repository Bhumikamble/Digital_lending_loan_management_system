
package com.miniSprint.Digital_lending_loan_management_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "borrower_profiles")
@Getter
@Setter
public class BorrowerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One borrower profile belongs to one user
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    @Column(name = "address", nullable = false)
    private String address;

    @NotBlank(message = "Employment type is required")
    @Column(name = "employment_type", nullable = false)
    private String employmentType;

    @NotNull(message = "Monthly income is required")
    @PositiveOrZero(message = "Income cannot be negative")
    @Column(
            name = "monthly_income",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal monthlyIncome;
}
