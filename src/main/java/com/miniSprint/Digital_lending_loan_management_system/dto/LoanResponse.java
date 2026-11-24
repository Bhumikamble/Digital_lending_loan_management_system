
package com.miniSprint.Digital_lending_loan_management_system.dto;

import com.miniSprint.Digital_lending_loan_management_system.entity.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanResponse {

    private Long id;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emi;
    private LoanStatus status;
    private LocalDate appliedDate;
    private String adminRemark;

    public LoanResponse() {
    }

    public LoanResponse(
            Long id,
            BigDecimal amount,
            BigDecimal interestRate,
            Integer tenureMonths,
            BigDecimal emi,
            LoanStatus status,
            LocalDate appliedDate,
            String adminRemark) {

        this.id = id;
        this.amount = amount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.emi = emi;
        this.status = status;
        this.appliedDate = appliedDate;
        this.adminRemark = adminRemark;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public BigDecimal getEmi() {
        return emi;
    }

    public void setEmi(BigDecimal emi) {
        this.emi = emi;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getAdminRemark() {
        return adminRemark;
    }

    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }
}
