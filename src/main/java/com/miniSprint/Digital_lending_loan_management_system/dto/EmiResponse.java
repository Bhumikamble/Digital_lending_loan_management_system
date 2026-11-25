package com.miniSprint.Digital_lending_loan_management_system.dto;

import java.math.BigDecimal;

public class EmiResponse {

    private BigDecimal monthlyEmi;
    private BigDecimal totalInterest;
    private BigDecimal totalPayment;

    public EmiResponse(
            BigDecimal monthlyEmi,
            BigDecimal totalInterest,
            BigDecimal totalPayment) {

        this.monthlyEmi = monthlyEmi;
        this.totalInterest = totalInterest;
        this.totalPayment = totalPayment;
    }

    public BigDecimal getMonthlyEmi() {
        return monthlyEmi;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }
}

