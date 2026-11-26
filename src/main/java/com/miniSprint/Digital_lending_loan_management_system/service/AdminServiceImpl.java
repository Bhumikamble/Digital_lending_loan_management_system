package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminDashboardResponse;
import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminLoanResponse;
import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminLoanDetailsResponse;
import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminRepaymentResponse;

import com.miniSprint.Digital_lending_loan_management_system.entity.Loan;
import com.miniSprint.Digital_lending_loan_management_system.entity.LoanStatus;
import com.miniSprint.Digital_lending_loan_management_system.entity.Repayment;
import com.miniSprint.Digital_lending_loan_management_system.entity.RepaymentStatus;

import com.miniSprint.Digital_lending_loan_management_system.exception.InvalidLoanStateException;
import com.miniSprint.Digital_lending_loan_management_system.exception.LoanNotFoundException;

import com.miniSprint.Digital_lending_loan_management_system.repository.LoanRepository;
import com.miniSprint.Digital_lending_loan_management_system.repository.RepaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final LoanRepository loanRepository;
    private final RepaymentService repaymentService;
    private final EmiCalculator emiCalculator;
    private final RepaymentRepository repaymentRepository;

    // Constructor Injection
    public AdminServiceImpl(
            LoanRepository loanRepository,
            RepaymentService repaymentService,
            EmiCalculator emiCalculator,
            RepaymentRepository repaymentRepository) {

        this.loanRepository = loanRepository;
        this.repaymentService = repaymentService;
        this.emiCalculator = emiCalculator;
        this.repaymentRepository = repaymentRepository;
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

    // 6. Retrieve Loan Details and Repayment Statistics
    @Override
    public AdminLoanDetailsResponse getLoanDetails(Long loanId) {

        Loan loan = findLoanById(loanId);

        List<Repayment> repayments =
                (loan.getStatus() == LoanStatus.APPROVED ||
                        loan.getStatus() == LoanStatus.CLOSED)
                        ? repaymentRepository
                        .findByLoanIdOrderByInstallmentNoAsc(loanId)
                        : List.of();

        LocalDate today = LocalDate.now();

        long totalInstallments = repayments.size();

        long paidInstallments = repayments.stream()
                .filter(r -> r.getStatus() == RepaymentStatus.PAID)
                .count();

        long pendingInstallments = repayments.stream()
                .filter(r -> r.getStatus() == RepaymentStatus.PENDING)
                .count();

        long overdueInstallments = repayments.stream()
                .filter(r -> r.getStatus() == RepaymentStatus.PENDING)
                .filter(r -> r.getDueDate().isBefore(today))
                .count();

        BigDecimal totalRepayableAmount = repayments.stream()
                .map(Repayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaidAmount = repayments.stream()
                .map(r -> r.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : r.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstandingAmount =
                totalRepayableAmount.subtract(totalPaidAmount);

        double repaymentProgress = totalInstallments == 0
                ? 0.0
                : (paidInstallments * 100.0) / totalInstallments;

        List<AdminRepaymentResponse> repaymentDetails =
                repayments.stream()
                        .map(r -> new AdminRepaymentResponse(
                                r.getId(),
                                r.getInstallmentNo(),
                                r.getDueDate(),
                                r.getPrincipalAmount(),
                                r.getInterestAmount(),
                                r.getAmount(),
                                r.getPaidAmount() == null
                                        ? BigDecimal.ZERO
                                        : r.getPaidAmount(),
                                r.getStatus().name(),
                                r.getPaidAt(),
                                r.getStatus() == RepaymentStatus.PENDING
                                        && r.getDueDate().isBefore(today)
                        ))
                        .toList();

        return new AdminLoanDetailsResponse(
                mapToResponse(loan),
                totalInstallments,
                paidInstallments,
                pendingInstallments,
                overdueInstallments,
                totalRepayableAmount,
                totalPaidAmount,
                outstandingAmount,
                repaymentProgress,
                repaymentDetails
        );
    }

    // 7. Find Loan by ID
    private Loan findLoanById(Long loanId) {

        return loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan not found with ID: " + loanId
                        ));
    }

    // 8. Validate Current Loan Status
    private void validateLoanStatus(Loan loan) {

        if (loan.getStatus() != LoanStatus.APPLIED) {

            throw new InvalidLoanStateException(
                    "Only APPLIED loans can be approved or rejected."
            );
        }
    }

    // 9. Convert Entity to DTO
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