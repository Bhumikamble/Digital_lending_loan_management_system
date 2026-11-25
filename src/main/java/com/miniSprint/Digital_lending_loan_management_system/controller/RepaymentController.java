
package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.service.RepaymentService;
import com.miniSprint.Digital_lending_loan_management_system.dto.EmiResponse;
import com.miniSprint.Digital_lending_loan_management_system.entity.User;
import com.miniSprint.Digital_lending_loan_management_system.entity.Loan;
import com.miniSprint.Digital_lending_loan_management_system.entity.Repayment;
import com.miniSprint.Digital_lending_loan_management_system.repository.UserRepository;
import com.miniSprint.Digital_lending_loan_management_system.repository.LoanRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/repayments")
public class RepaymentController {

    private final RepaymentService repaymentService;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    // Constructor injection
    public RepaymentController(
            RepaymentService repaymentService,
            UserRepository userRepository,
            LoanRepository loanRepository) {

        this.repaymentService = repaymentService;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    // Get logged-in borrower
    private User getLoggedInUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Get borrower's loans
    private List<Loan> getBorrowerLoans(User user) {

        return loanRepository.findByBorrower_User_Id(user.getId());
    }

    // Get selected loan ID
    private Long getSelectedLoanId(
            User user,
            HttpSession session,
            Model model) {

        List<Loan> loans = getBorrowerLoans(user);

        model.addAttribute("loans", loans);

        if (loans.isEmpty()) {
            return null;
        }

        Long selectedLoanId =
                (Long) session.getAttribute("selectedLoanId");

        if (selectedLoanId != null) {

            boolean belongsToUser = loans.stream()
                    .anyMatch(loan ->
                            loan.getId().equals(selectedLoanId));

            if (belongsToUser) {
                return selectedLoanId;
            }

            session.removeAttribute("selectedLoanId");
        }

        // Automatically select loan if borrower has only one
        if (loans.size() == 1) {

            Long loanId = loans.get(0).getId();

            session.setAttribute("selectedLoanId", loanId);

            return loanId;
        }

        return null;
    }

    // 1. Show EMI Calculator
    @GetMapping("/emi")
    public String showEmiCalculator(
            Authentication authentication,
            HttpSession session,
            Model model) {

        User user = getLoggedInUser(authentication);

        Long loanId = getSelectedLoanId(user, session, model);

        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("loanId", loanId);
        model.addAttribute("currentPage", "emi");

        return "borrower/emi-calculator";
    }

    // 2. Select loan when borrower has multiple loans
    @PostMapping("/select-loan")
    public String selectLoan(
            @RequestParam Long loanId,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        Loan loan = loanRepository
                .findByIdAndBorrower_User_Id(loanId, user.getId())
                .orElse(null);

        if (loan == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid loan selection");

            return "redirect:/repayments/emi";
        }

        session.setAttribute("selectedLoanId", loanId);

        return "redirect:/repayments/emi";
    }

    // 3. Calculate EMI
    @PostMapping("/emi/calculate")
    public String calculateEMI(
            @RequestParam BigDecimal loanAmount,
            @RequestParam BigDecimal interestRate,
            @RequestParam int tenureMonths,
            Authentication authentication,
            HttpSession session,
            Model model) {

        User user = getLoggedInUser(authentication);

        Long loanId = getSelectedLoanId(user, session, model);

        EmiResponse result = repaymentService.calculateEMI(
                loanAmount,
                interestRate,
                tenureMonths
        );

        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("emiResult", result);
        model.addAttribute("loanAmount", loanAmount);
        model.addAttribute("interestRate", interestRate);
        model.addAttribute("tenureMonths", tenureMonths);
        model.addAttribute("loanId", loanId);
        model.addAttribute("currentPage", "emi");

        return "borrower/emi-calculator";
    }

    // 4. Generate repayment schedule
    @PostMapping("/generate")
    public String generateSchedule(
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        Long loanId =
                (Long) session.getAttribute("selectedLoanId");

        if (loanId == null ||
                loanRepository.findByIdAndBorrower_User_Id(
                        loanId, user.getId()).isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a valid loan");

            return "redirect:/repayments/emi";
        }

        try {

            repaymentService.generateSchedule(
                    loanId,
                    user.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Repayment schedule generated successfully!");

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/repayments/schedule";
    }

    // 5. View repayment schedule
    @GetMapping("/schedule")
    public String showSchedule(
            Authentication authentication,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        // Automatically select loan if borrower has only one
        Long loanId = getSelectedLoanId(user, session, model);

        if (loanId == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a loan first");

            return "redirect:/repayments/emi";
        }

        // Verify loan ownership
        Loan loan = loanRepository
                .findByIdAndBorrower_User_Id(
                        loanId,
                        user.getId())
                .orElse(null);

        if (loan == null) {

            session.removeAttribute("selectedLoanId");

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid loan selection");

            return "redirect:/repayments/emi";
        }

        // Get repayment records
        List<Repayment> repayments =
                repaymentService.getRepaymentHistory(
                        loanId,
                        user.getId());

        // Pass data to Thymeleaf
        model.addAttribute("loan", loan);
        model.addAttribute("repayments", repayments);
        model.addAttribute("scheduleExists", !repayments.isEmpty());
        model.addAttribute("loanId", loanId);
        model.addAttribute("currentPage", "schedule");
        model.addAttribute("userEmail", user.getEmail());

        return "borrower/repayment-schedule";
    }

    // 6. Mark installment as paid
    @PostMapping("/{repaymentId}/pay")
    public String markAsPaid(
            @PathVariable Long repaymentId,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        Long loanId =
                (Long) session.getAttribute("selectedLoanId");

        if (loanId == null ||
                loanRepository.findByIdAndBorrower_User_Id(
                        loanId, user.getId()).isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a loan first");

            return "redirect:/repayments/emi";
        }

        try {

            repaymentService.markAsPaid(
                    repaymentId,
                    user.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Installment paid successfully!");

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/repayments/schedule";
    }

    // 7. View repayment history
    @GetMapping("/history")
    public String showRepaymentHistory(
            Authentication authentication,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        Long loanId = getSelectedLoanId(user, session, model);

        if (loanId == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a loan first");

            return "redirect:/repayments/emi";
        }

        // Verify loan ownership
        Loan loan = loanRepository
                .findByIdAndBorrower_User_Id(
                        loanId,
                        user.getId())
                .orElse(null);

        if (loan == null) {

            session.removeAttribute("selectedLoanId");

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid loan selection");

            return "redirect:/repayments/emi";
        }

        List<Repayment> repayments =
                repaymentService.getRepaymentHistory(
                        loanId,
                        user.getId());

        model.addAttribute("repayments", repayments);
        model.addAttribute("loanId", loanId);
        model.addAttribute("currentPage", "history");
        model.addAttribute("userEmail", user.getEmail());

        return "borrower/repayment-history";
    }
}
