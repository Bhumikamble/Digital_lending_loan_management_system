package com.miniSprint.Digital_lending_loan_management_system.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmiCalculatorTest {

    private final EmiCalculator emiCalculator = new EmiCalculator();

    @Test
    void calculatesEmiForInterestBearingLoan() {
        BigDecimal emi = emiCalculator.calculateEMI(
                new BigDecimal("100000"), new BigDecimal("12"), 12);

        assertEquals(new BigDecimal("8884.88"), emi);
    }

    @Test
    void calculatesEqualInstallmentsForZeroInterestLoan() {
        BigDecimal emi = emiCalculator.calculateEMI(
                new BigDecimal("100000"), BigDecimal.ZERO, 12);

        assertEquals(new BigDecimal("8333.33"), emi);
    }

    @Test
    void rejectsInvalidLoanCalculationDetails() {
        assertThrows(IllegalArgumentException.class, () ->
                emiCalculator.calculateEMI(BigDecimal.ZERO, new BigDecimal("12"), 12));
        assertThrows(IllegalArgumentException.class, () ->
                emiCalculator.calculateEMI(new BigDecimal("100000"), new BigDecimal("-1"), 12));
        assertThrows(IllegalArgumentException.class, () ->
                emiCalculator.calculateEMI(new BigDecimal("100000"), new BigDecimal("12"), 0));
    }
}

