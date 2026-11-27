package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.dto.EmiResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepaymentServiceTest {

    private final RepaymentService repaymentService =
            new RepaymentService(null, null, new EmiCalculator());

    @Test
    void returnsEmiAndLoanTotals() {
        EmiResponse response = repaymentService.calculateEMI(
                new BigDecimal("100000"), new BigDecimal("12"), 12);

        assertEquals(new BigDecimal("8884.88"), response.getMonthlyEmi());
        assertEquals(new BigDecimal("106618.56"), response.getTotalPayment());
        assertEquals(new BigDecimal("6618.56"), response.getTotalInterest());
    }
}