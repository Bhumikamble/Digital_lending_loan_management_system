
package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.dto.LoanApplicationRequest;
import com.miniSprint.Digital_lending_loan_management_system.dto.LoanResponse;
import com.miniSprint.Digital_lending_loan_management_system.entity.Loan;
import com.miniSprint.Digital_lending_loan_management_system.service.LoanService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // Apply for a loan
    @PostMapping
    public ResponseEntity<LoanResponse> applyForLoan(
            @RequestParam Long userId,
            @Valid @RequestBody LoanApplicationRequest request) {

        Loan loan = loanService.applyForLoan(
                userId,
                request.getAmount(),
                request.getTenureMonths()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(loan));
    }

    // Get all loans of borrower
    @GetMapping
    public ResponseEntity<List<LoanResponse>> getMyLoans(
            @RequestParam Long userId) {

        List<LoanResponse> loans = loanService.getMyLoans(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(loans);
    }

    // Get a particular loan
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(
            @PathVariable Long id,
            @RequestParam Long userId) {

        Loan loan = loanService.getMyLoanById(id, userId);

        return ResponseEntity.ok(convertToResponse(loan));
    }

    // Convert Entity into Response DTO
    private LoanResponse convertToResponse(Loan loan) {

        return new LoanResponse(
                loan.getId(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getEmi(),
                loan.getStatus(),
                loan.getAppliedDate(),
                loan.getAdminRemark()
        );
    }
}
