package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminDashboardResponse;
import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminLoanResponse;
import com.miniSprint.Digital_lending_loan_management_system.entity.Loan;
import com.miniSprint.Digital_lending_loan_management_system.entity.LoanStatus;
import com.miniSprint.Digital_lending_loan_management_system.exception.InvalidLoanStateException;
import com.miniSprint.Digital_lending_loan_management_system.exception.LoanNotFoundException;
import com.miniSprint.Digital_lending_loan_management_system.repository.LoanRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final LoanRepository loanRepository;
    private final RepaymentService repaymentService;
    private final EmiCalculator emiCalculator;

    // Constructor Injection
    public AdminServiceImpl(
            LoanRepository loanRepository,
            RepaymentService repaymentService,
            EmiCalculator emiCalculator) {

        this.loanRepository = loanRepository;
        this.repaymentService = repaymentService;
        this.emiCalculator = emiCalculator;
    }

    // 1. Dashboard Statistics
    @Override
    public AdminDashboardResponse getDashboardStats() {

        List<Loan> loans = loanRepository.findAll();

        long total = loans.size();

        long pending = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.APPLIED)
                .count();

        long approved = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.APPROVED)
                .count();

        long rejected = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.REJECTED)
                .count();

        long closed = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.CLOSED)
                .count();

        return new AdminDashboardResponse(
                total,
                pending,
                approved,
                rejected,
                closed
        );
    }

    // 2. Retrieve All Loans
    @Override
    public List<AdminLoanResponse> getAllLoans() {

        return loanRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 3. Filter Loans by Status
    @Override
    public List<AdminLoanResponse> getLoansByStatus(String status) {

        if (status == null || status.isBlank()) {
            return getAllLoans();
        }

        LoanStatus loanStatus;

        try {
            loanStatus = LoanStatus.valueOf(
                    status.trim().toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid loan status: " + status
            );
        }

        return loanRepository.findAll()
                .stream()
                .filter(loan -> loan.getStatus() == loanStatus)
                .map(this::mapToResponse)
                .toList();
    }

    // 4. Approve Loan
    @Override
    @Transactional
    public AdminLoanResponse approveLoan(Long loanId) {

        Loan loan = findLoanById(loanId);

        validateLoanStatus(loan);

        BigDecimal emi = emiCalculator.calculateEMI(
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths()
        );

        loan.setEmi(emi);
        loan.setStatus(LoanStatus.APPROVED);
        loan.setAdminRemark(null);

        Loan savedLoan = loanRepository.save(loan);

        // Generate repayment schedule
        repaymentService.generateSchedule(savedLoan.getId());

        return mapToResponse(savedLoan);
    }

    // 5. Reject Loan
    @Override
    @Transactional
    public AdminLoanResponse rejectLoan(
            Long loanId,
            String rejectionRemark) {

        if (rejectionRemark == null ||
                rejectionRemark.isBlank()) {

            throw new IllegalArgumentException(
                    "Rejection remark is required."
            );
        }

        if (rejectionRemark.length() > 500) {

            throw new IllegalArgumentException(
                    "Remark cannot exceed 500 characters."
            );
        }

        Loan loan = findLoanById(loanId);

        validateLoanStatus(loan);

        loan.setStatus(LoanStatus.REJECTED);
        loan.setAdminRemark(rejectionRemark.trim());

        Loan savedLoan = loanRepository.save(loan);

        return mapToResponse(savedLoan);
    }

    // 6. Find Loan by ID
    private Loan findLoanById(Long loanId) {

        return loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan not found with ID: " + loanId
                        ));
    }

    // 7. Validate Current Loan Status
    private void validateLoanStatus(Loan loan) {

        if (loan.getStatus() != LoanStatus.APPLIED) {

            throw new InvalidLoanStateException(
                    "Only APPLIED loans can be approved or rejected."
            );
        }
    }

    // 8. Convert Entity to DTO
    private AdminLoanResponse mapToResponse(Loan loan) {

        return new AdminLoanResponse(
                loan.getId(),
                loan.getBorrower().getFullName(),
                loan.getBorrower().getMonthlyIncome(),
                loan.getBorrower().getEmploymentType(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getEmi(),
                loan.getStatus().name(),
                loan.getAdminRemark()
        );
    }
}
