
package com.miniSprint.Digital_lending_loan_management_system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class LoanApplicationRequest {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00",
            message = "Minimum loan amount is 1000")
    @DecimalMax(value = "1000000.00",
            message = "Maximum loan amount is 1000000")
    private BigDecimal amount;

    @NotNull(message = "Loan tenure is required")
    @Min(value = 3,
            message = "Minimum tenure is 3 months")
    @Max(value = 60,
            message = "Maximum tenure is 60 months")
    private Integer tenureMonths;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }
}
