package com.miniSprint.Digital_lending_loan_management_system.service;



import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EmiCalculator {

    public BigDecimal calculateEMI(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            int months) {

        if (principal == null ||
                annualInterestRate == null ||
                principal.signum() <= 0 ||
                annualInterestRate.signum() < 0 ||
                months <= 0) {

            throw new IllegalArgumentException(
                    "Invalid loan calculation details");
        }

        // Monthly interest rate
        BigDecimal monthlyRate = annualInterestRate.divide(
                BigDecimal.valueOf(1200),
                12,
                RoundingMode.HALF_UP
        );

        // Zero interest calculation
        if (monthlyRate.signum() == 0) {
            return principal.divide(
                    BigDecimal.valueOf(months),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        // (1 + R)^N
        BigDecimal factor = BigDecimal.ONE
                .add(monthlyRate)
                .pow(months);

        // EMI formula
        BigDecimal numerator = principal
                .multiply(monthlyRate)
                .multiply(factor);

        BigDecimal denominator = factor.subtract(
                BigDecimal.ONE);

        return numerator.divide(
                denominator,
                2,
                RoundingMode.HALF_UP
        );
    }
}