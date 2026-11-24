package com.miniSprint.Digital_lending_loan_management_system.service;



import com.miniSprint.Digital_lending_loan_management_system.repository.RepaymentRepository;
import com.sprint.lending.entity.*;
import com.sprint.lending.repository.*;
import com.sprint.lending.dto.EmiResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final LoanRepository loanRepository;
    private final EmiCalculator emiCalculator;

    public RepaymentService(
            RepaymentRepository repaymentRepository,
            LoanRepository loanRepository,
            EmiCalculator emiCalculator) {

        this.repaymentRepository = repaymentRepository;
        this.loanRepository = loanRepository;
        this.emiCalculator = emiCalculator;
    }

    // 1. Calculate EMI
    public EmiResponse calculateEMI(
            BigDecimal amount,
            BigDecimal interestRate,
            int months) {

        BigDecimal emi = emiCalculator.calculateEMI(
                amount, interestRate, months);

        BigDecimal totalPayment = emi.multiply(
                BigDecimal.valueOf(months));

        BigDecimal totalInterest = totalPayment.subtract(amount);

        return new EmiResponse(
                emi,
                totalInterest,
                totalPayment
        );
    }

    // 2. Generate monthly repayment schedule
    @Transactional
    public List<Repayment> generateSchedule(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (repaymentRepository.existsByLoanId(loanId)) {
            throw new IllegalStateException(
                    "Repayment schedule already exists");
        }

        BigDecimal principal = loan.getAmount();

        BigDecimal annualRate = loan.getInterestRate();

        int months = loan.getTenureMonths();

        BigDecimal emi = emiCalculator.calculateEMI(
                principal, annualRate, months);

        BigDecimal monthlyRate = annualRate.divide(
                BigDecimal.valueOf(1200),
                12,
                RoundingMode.HALF_UP
        );

        BigDecimal remainingPrincipal = principal;

        LocalDate firstDueDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= months; i++) {

            BigDecimal interest = remainingPrincipal
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalPart = emi.subtract(interest);

            // Adjust the final instalment for rounding
            if (i == months) {
                principalPart = remainingPrincipal;
            }

            BigDecimal installmentAmount = principalPart
                    .add(interest)
                    .setScale(2, RoundingMode.HALF_UP);

            Repayment repayment = new Repayment();

            repayment.setInstallmentNo(i);

            repayment.setDueDate(firstDueDate.plusMonths(i - 1));

            repayment.setPrincipalAmount(principalPart);

            repayment.setInterestAmount(interest);

            repayment.setAmount(installmentAmount);

            repayment.setPaidAmount(BigDecimal.ZERO);

            repayment.setStatus(RepaymentStatus.PENDING);

            repayment.setLoan(loan);

            repaymentRepository.save(repayment);

            remainingPrincipal = remainingPrincipal.subtract(
                    principalPart);
        }

        return repaymentRepository
                .findByLoanIdOrderByInstallmentNoAsc(loanId);
    }

    // 3. Retrieve repayment history
    public List<Repayment> getRepaymentHistory(Long loanId) {

        return repaymentRepository
                .findByLoanIdOrderByInstallmentNoAsc(loanId);
    }

    // 4. Mark instalment as paid
    @Transactional
    public void markAsPaid(Long repaymentId) {

        Repayment repayment = repaymentRepository
                .findById(repaymentId)
                .orElseThrow(() ->
                        new RuntimeException("Repayment not found"));

        if (repayment.getStatus() != RepaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "This instalment cannot be marked as paid");
        }

        if (repayment.getDueDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException(
                    "This instalment is not due yet");
        }

        repayment.setStatus(RepaymentStatus.PAID);

        repayment.setPaidAmount(repayment.getAmount());

        repayment.setPaidAt(LocalDateTime.now());

        repaymentRepository.save(repayment);
    }
}