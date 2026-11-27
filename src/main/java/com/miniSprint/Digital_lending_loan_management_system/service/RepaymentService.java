
package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.entity.*;
import com.miniSprint.Digital_lending_loan_management_system.repository.*;
import com.miniSprint.Digital_lending_loan_management_system.dto.EmiResponse;

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

    // Constructor injection
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
                amount,
                interestRate,
                months
        );

        BigDecimal totalPayment = emi.multiply(
                BigDecimal.valueOf(months)
        );

        BigDecimal totalInterest = totalPayment.subtract(amount);

        return new EmiResponse(
                emi,
                totalInterest,
                totalPayment
        );
    }

    // 2A. Generate repayment schedule for ADMIN
    // Keeps the existing AdminServiceImpl call working.
    @Transactional
    public List<Repayment> generateSchedule(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        return createSchedule(loan);
    }

    // 2B. Generate repayment schedule for BORROWER
    // Verifies that the loan belongs to the logged-in borrower.
    @Transactional
    public List<Repayment> generateSchedule(
            Long loanId, Long userId) {

        Loan loan = loanRepository
                .findByIdAndBorrower_User_Id(loanId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        return createSchedule(loan);
    }

    // 3. Common schedule-generation logic
    private List<Repayment> createSchedule(Loan loan) {

        Long loanId = loan.getId();

        // Only approved loans can have a repayment schedule
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException(
                    "Repayment schedule can only be generated for approved loans."
            );
        }

        // Check whether schedule already exists
        if (repaymentRepository.existsByLoanId(loanId)) {
            throw new IllegalStateException(
                    "Repayment schedule already exists."
            );
        }

        // Get loan details
        BigDecimal principal = loan.getAmount();
        BigDecimal annualRate = loan.getInterestRate();
        int months = loan.getTenureMonths();

        // Calculate EMI
        BigDecimal emi = emiCalculator.calculateEMI(
                principal,
                annualRate,
                months
        );

        // Calculate monthly interest rate
        BigDecimal monthlyRate = annualRate.divide(
                BigDecimal.valueOf(1200),
                12,
                RoundingMode.HALF_UP
        );

        BigDecimal remainingPrincipal = principal;

        // First installment due date
        LocalDate firstDueDate = LocalDate.now().plusMonths(1);

        // Generate monthly installments
        for (int i = 1; i <= months; i++) {

            // Calculate monthly interest
            BigDecimal interest = remainingPrincipal
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate principal component
            BigDecimal principalPart = emi.subtract(interest);

            // Adjust final installment for rounding
            if (i == months) {
                principalPart = remainingPrincipal;
            }

            // Calculate installment amount
            BigDecimal installmentAmount = principalPart
                    .add(interest)
                    .setScale(2, RoundingMode.HALF_UP);

            // Create repayment object
            Repayment repayment = new Repayment();

            repayment.setInstallmentNo(i);

            repayment.setDueDate(
                    firstDueDate.plusMonths(i - 1)
            );

            repayment.setPrincipalAmount(principalPart);

            repayment.setInterestAmount(interest);

            repayment.setAmount(installmentAmount);

            repayment.setPaidAmount(BigDecimal.ZERO);

            repayment.setStatus(RepaymentStatus.PENDING);

            repayment.setLoan(loan);

            // Save installment
            repaymentRepository.save(repayment);

            // Update remaining principal
            remainingPrincipal = remainingPrincipal.subtract(
                    principalPart
            );
        }

        // Return repayment schedule
        return repaymentRepository
                .findByLoanIdOrderByInstallmentNoAsc(loanId);
    }

    // 4. Retrieve repayment history
    // Verifies that the loan belongs to the logged-in borrower.
    public List<Repayment> getRepaymentHistory(
            Long loanId, Long userId) {

        loanRepository.findByIdAndBorrower_User_Id(loanId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        return repaymentRepository
                .findByLoanIdOrderByInstallmentNoAsc(loanId);
    }

    // 5. Mark installment as paid
    // Verifies ownership, approval status and installment order.
    @Transactional
    public void markAsPaid(Long repaymentId, Long userId) {

        Repayment repayment = repaymentRepository
                .findByIdAndLoan_Borrower_User_Id(
                        repaymentId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Repayment not found"));

        // Only approved loans can receive EMI payments
        if (repayment.getLoan().getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException(
                    "EMI payments are allowed only for approved loans."
            );
        }

        // Check repayment status
        if (repayment.getStatus() != RepaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "This installment cannot be marked as paid"
            );
        }


        // Allow early EMI payments in installment order
        Repayment nextPayable = repaymentRepository
                .findByLoanIdOrderByInstallmentNoAsc(
                        repayment.getLoan().getId())
                .stream()
                .filter(r -> r.getStatus() == RepaymentStatus.PENDING)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No pending installments found"));

        if (!nextPayable.getId().equals(repayment.getId())) {
            throw new IllegalStateException(
                    "Please pay the previous installment first"
            );
        }

        // Update repayment details
        repayment.setStatus(RepaymentStatus.PAID);

        repayment.setPaidAmount(repayment.getAmount());

        repayment.setPaidAt(LocalDateTime.now());

        // Save updated repayment
        repaymentRepository.save(repayment);
    }
}
