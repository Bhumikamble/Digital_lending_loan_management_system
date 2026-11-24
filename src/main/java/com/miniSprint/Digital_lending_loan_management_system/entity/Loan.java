
package com.miniSprint.Digital_lending_loan_management_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many loans can belong to one borrower
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private BorrowerProfile borrower;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @PositiveOrZero(message = "Interest rate cannot be negative")
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @NotNull(message = "Loan tenure is required")
    @Min(value = 1, message = "Minimum tenure is 1 month")
    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @NotBlank(message = "Loan purpose is required")
    @Column(name = "loan_purpose", nullable = false, length = 255)
    private String loanPurpose;

    @NotNull(message = "EMI is required")
    @Positive(message = "EMI must be greater than zero")
    @Column(precision = 12, scale = 2)
    private BigDecimal emi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;

    // Automatically set initial values before saving
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = LoanStatus.PENDING;
        }

        if (appliedDate == null) {
            appliedDate = LocalDate.now();
        }
    }
}
