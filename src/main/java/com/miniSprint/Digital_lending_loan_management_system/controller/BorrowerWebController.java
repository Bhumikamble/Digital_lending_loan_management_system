package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.entity.Loan;
import com.miniSprint.Digital_lending_loan_management_system.entity.LoanStatus;
import com.miniSprint.Digital_lending_loan_management_system.entity.User;
import com.miniSprint.Digital_lending_loan_management_system.repository.UserRepository;
import com.miniSprint.Digital_lending_loan_management_system.service.LoanService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/borrower")
public class BorrowerWebController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    // Constructor injection
    public BorrowerWebController(
            LoanService loanService,
            UserRepository userRepository) {

        this.loanService = loanService;
        this.userRepository = userRepository;
    }

    // Borrower Dashboard
    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // Step 1: Get the logged-in borrower
        User user = getLoggedInUser(userDetails);

        // Step 2: Get all loans belonging to this borrower
        List<Loan> loans = loanService.getMyLoans(user.getId());

        // Step 3: Calculate dashboard statistics
        long totalLoans = loans.size();

        long approvedLoans = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.APPROVED)
                .count();

        long pendingLoans = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.APPLIED)
                .count();

        // Step 4: Get the 5 most recent loan applications
        List<Loan> recentLoans = loans.stream()
                .sorted(Comparator.comparing(
                        Loan::getAppliedDate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(5)
                .toList();

        // Step 5: Send data to dashboard.html
        model.addAttribute("email", user.getEmail());
        model.addAttribute("totalLoans", totalLoans);
        model.addAttribute("approvedLoans", approvedLoans);
        model.addAttribute("pendingLoans", pendingLoans);
        model.addAttribute("recentLoans", recentLoans);

        // Step 6: Open the Thymeleaf page
        return "borrower/dashboard";
    }

    // Helper method: Find logged-in user using email
    private User getLoggedInUser(UserDetails userDetails) {

        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }
}