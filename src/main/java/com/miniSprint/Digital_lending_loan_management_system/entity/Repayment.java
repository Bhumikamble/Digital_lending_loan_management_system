
package com.miniSprint.Digital_lending_loan_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "repayments")
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer installmentNo;

    private LocalDate dueDate;

    private BigDecimal principalAmount;

    private BigDecimal interestAmount;

    private BigDecimal amount;

    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    private RepaymentStatus status;

    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    public Repayment() {
    }
}
