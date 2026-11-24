
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
    @DecimalMin(value = "1000.00",
            message = "Minimum loan amount is 1000")
    @DecimalMax(value = "1000000.00",
            message = "Maximum loan amount is 1000000")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @PositiveOrZero(message = "Interest rate cannot be negative")
    @Column(name = "interest_rate",
            nullable = false,
            precision = 5,
            scale = 2)
    private BigDecimal interestRate;

    @NotNull(message = "Loan tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    @Max(value = 60, message = "Maximum tenure is 60 months")
    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @NotNull(message = "EMI is required")
    @Positive(message = "EMI must be greater than zero")
    @Column(precision = 12, scale = 2)
    private BigDecimal emi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;

    @Column(name = "admin_remark", length = 500)
    private String adminRemark;

    // Automatically initialize loan details
    @PrePersist
    public void prePersist() {

        if (status == null) {
            status = LoanStatus.APPLIED;
        }

        if (appliedDate == null) {
            appliedDate = LocalDate.now();
        }
    }
}
