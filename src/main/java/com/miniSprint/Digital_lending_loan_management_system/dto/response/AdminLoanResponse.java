package com.miniSprint.Digital_lending_loan_management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoanResponse {

    private Long loanId;
    private String borrowerName;
    private BigDecimal monthlyIncome;
    private String employmentType;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emi;
    private String status;
    private String rejectionRemark;
}