
package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.entity.BorrowerProfile;
import com.miniSprint.Digital_lending_loan_management_system.entity.Loan;
import com.miniSprint.Digital_lending_loan_management_system.entity.LoanStatus;
import com.miniSprint.Digital_lending_loan_management_system.repository.BorrowerProfileRepository;
import com.miniSprint.Digital_lending_loan_management_system.repository.LoanRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BorrowerProfileRepository borrowerProfileRepository;

    @Value("${loan.default-interest-rate:12.00}")
    private BigDecimal defaultInterestRate;

    private static final BigDecimal EMI_INCOME_LIMIT =
            new BigDecimal("0.40");

    public LoanService(
            LoanRepository loanRepository,
            BorrowerProfileRepository borrowerProfileRepository) {
        this.loanRepository = loanRepository;
        this.borrowerProfileRepository = borrowerProfileRepository;
    }

    // Apply for a loan
    @Transactional
    public Loan applyForLoan(
            Long userId,
            BigDecimal amount,
            Integer tenureMonths) {

        BorrowerProfile borrower = borrowerProfileRepository
                .findByUser_Id(userId)
                .orElseThrow(() ->
                        new RuntimeException("Borrower profile not found"));

        if (amount == null ||
                amount.compareTo(new BigDecimal("1000")) < 0 ||
                amount.compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException(
                    "Loan amount must be between 1000 and 1000000");
        }

        if (tenureMonths == null ||
                tenureMonths < 3 || tenureMonths > 60) {
            throw new IllegalArgumentException(
                    "Tenure must be between 3 and 60 months");
        }

        boolean appliedLoan = loanRepository
                .existsByBorrower_IdAndStatus(
                        borrower.getId(), LoanStatus.APPLIED);

        boolean approvedLoan = loanRepository
                .existsByBorrower_IdAndStatus(
                        borrower.getId(), LoanStatus.APPROVED);

        if (appliedLoan || approvedLoan) {
            throw new IllegalArgumentException(
                    "You already have an active loan");
        }

        BigDecimal emi = calculateEMI(
                amount, defaultInterestRate, tenureMonths);

        BigDecimal maximumEMI = borrower.getMonthlyIncome()
                .multiply(EMI_INCOME_LIMIT);

        if (emi.compareTo(maximumEMI) > 0) {
            throw new IllegalArgumentException(
                    "Insufficient monthly income for this loan");
        }

        Loan loan = new Loan();

        loan.setBorrower(borrower);
        loan.setAmount(amount);
        loan.setInterestRate(defaultInterestRate);
        loan.setTenureMonths(tenureMonths);
        loan.setEmi(emi);
        loan.setStatus(LoanStatus.APPLIED);

        return loanRepository.save(loan);
    }

    // Calculate monthly EMI
    public BigDecimal calculateEMI(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            int tenureMonths) {

        BigDecimal monthlyRate = annualInterestRate
                .divide(new BigDecimal("1200"), 12,
                        RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(
                    BigDecimal.valueOf(tenureMonths),
                    2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRate =
                BigDecimal.ONE.add(monthlyRate);

        BigDecimal power = onePlusRate.pow(tenureMonths);

        BigDecimal numerator =
                principal.multiply(monthlyRate).multiply(power);

        BigDecimal denominator =
                power.subtract(BigDecimal.ONE);

        return numerator.divide(
                denominator, 2, RoundingMode.HALF_UP);
    }

    // Get all loans of a borrower
    @Transactional(readOnly = true)
    public List<Loan> getMyLoans(Long userId) {

        return loanRepository.findByBorrower_User_Id(userId);
    }

    // Get a particular loan
    @Transactional(readOnly = true)
    public Loan getMyLoanById(Long loanId, Long userId) {

        return loanRepository
                .findByIdAndBorrower_User_Id(loanId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));
    }
}
