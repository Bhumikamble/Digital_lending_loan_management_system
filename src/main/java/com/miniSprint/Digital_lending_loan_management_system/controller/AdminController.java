package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.dto.request.LoanRejectionRequest;
import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminDashboardResponse;
import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminLoanResponse;
import com.miniSprint.Digital_lending_loan_management_system.service.AdminService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    // Constructor Injection
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 1. Get Dashboard Statistics
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {

        return ResponseEntity.ok(
                adminService.getDashboardStats()
        );
    }

    // 2. Get All Loans or Filter by Status
    @GetMapping("/loans")
    public ResponseEntity<List<AdminLoanResponse>> getLoans(
            @RequestParam(required = false) String status) {

        if (status == null || status.isBlank()) {

            return ResponseEntity.ok(
                    adminService.getAllLoans()
            );
        }

        return ResponseEntity.ok(
                adminService.getLoansByStatus(status)
        );
    }

    // 3. Approve Loan
    @PutMapping("/loans/{id}/approve")
    public ResponseEntity<AdminLoanResponse> approveLoan(
            @PathVariable("id") Long loanId) {

        return ResponseEntity.ok(
                adminService.approveLoan(loanId)
        );
    }

    // 4. Reject Loan
    @PutMapping("/loans/{id}/reject")
    public ResponseEntity<AdminLoanResponse> rejectLoan(
            @PathVariable("id") Long loanId,
            @Valid @RequestBody LoanRejectionRequest request) {

        return ResponseEntity.ok(
                adminService.rejectLoan(
                        loanId,
                        request.getRejectionRemark()
                )
        );
    }
}
