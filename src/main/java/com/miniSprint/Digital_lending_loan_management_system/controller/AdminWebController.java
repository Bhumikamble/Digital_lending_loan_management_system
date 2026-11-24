package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.exception.InvalidLoanStateException;
import com.miniSprint.Digital_lending_loan_management_system.exception.LoanNotFoundException;
import com.miniSprint.Digital_lending_loan_management_system.service.AdminService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final AdminService adminService;

    // Constructor Injection
    public AdminWebController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 1. Display Admin Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {

        model.addAttribute(
                "dashboard",
                adminService.getDashboardStats()
        );

        return "admin/dashboard";
    }

    // 2. Display and Filter Loan Applications
    @GetMapping("/loans")
    public String showLoanApplications(
            @RequestParam(required = false) String status,
            Model model) {

        String selectedStatus = status == null
                ? ""
                : status.trim().toUpperCase(Locale.ROOT);

        try {

            if (selectedStatus.isBlank()) {

                model.addAttribute(
                        "loans",
                        adminService.getAllLoans()
                );

            } else {

                model.addAttribute(
                        "loans",
                        adminService.getLoansByStatus(selectedStatus)
                );
            }

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    "Invalid loan status."
            );

            model.addAttribute(
                    "loans",
                    adminService.getAllLoans()
            );

            selectedStatus = "";
        }

        model.addAttribute("selectedStatus", selectedStatus);

        return "admin/loan-applications";
    }

    // 3. Approve Loan Application
    @PostMapping("/loans/approve")
    public String approveLoan(
            @RequestParam Long loanId,
            RedirectAttributes redirectAttributes) {

        try {

            adminService.approveLoan(loanId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Loan #" + loanId +
                            " approved successfully. Repayment schedule generated."
            );

        } catch (LoanNotFoundException |
                 InvalidLoanStateException |
                 IllegalArgumentException |
                 IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/admin/loans";
    }

    // 4. Reject Loan Application
    @PostMapping("/loans/reject")
    public String rejectLoan(
            @RequestParam Long loanId,
            @RequestParam String rejectionRemark,
            RedirectAttributes redirectAttributes) {

        try {

            if (rejectionRemark.isBlank()) {

                throw new IllegalArgumentException(
                        "Rejection remark is required."
                );
            }

            if (rejectionRemark.length() > 500) {

                throw new IllegalArgumentException(
                        "Remark cannot exceed 500 characters."
                );
            }

            adminService.rejectLoan(
                    loanId,
                    rejectionRemark.trim()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Loan #" + loanId + " rejected successfully."
            );

        } catch (LoanNotFoundException |
                 InvalidLoanStateException |
                 IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/admin/loans";
    }
}
