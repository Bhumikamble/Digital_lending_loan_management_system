
package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.service.RepaymentService;
import com.miniSprint.Digital_lending_loan_management_system.dto.EmiResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/repayments")
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    // EMI Calculator page
    @GetMapping("/emi")
    public String showEmiCalculator(
            @RequestParam(required = false) Long loanId,
            Model model) {

        model.addAttribute("loanId", loanId);
        model.addAttribute("currentPage", "emi");

        return "borrower/emi-calculator";
    }

    // Calculate EMI
    @PostMapping("/emi/calculate")
    public String calculateEMI(
            @RequestParam BigDecimal loanAmount,
            @RequestParam BigDecimal interestRate,
            @RequestParam int tenureMonths,
            @RequestParam(required = false) Long loanId,
            Model model) {

        EmiResponse result = repaymentService.calculateEMI(
                loanAmount,
                interestRate,
                tenureMonths
        );

        model.addAttribute("emiResult", result);
        model.addAttribute("loanAmount", loanAmount);
        model.addAttribute("interestRate", interestRate);
        model.addAttribute("tenureMonths", tenureMonths);
        model.addAttribute("loanId", loanId);
        model.addAttribute("currentPage", "emi");

        return "borrower/emi-calculator";
    }

    // Generate repayment schedule
    @PostMapping("/generate")
    public String generateSchedule(
            @RequestParam Long loanId,
            RedirectAttributes redirectAttributes) {

        try {
            repaymentService.generateSchedule(loanId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Repayment schedule generated successfully!"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/repayments/schedule?loanId=" + loanId;
    }

    // View repayment schedule
    @GetMapping("/schedule")
    public String showSchedule(
            @RequestParam Long loanId,
            Model model) {

        model.addAttribute(
                "repayments",
                repaymentService.getRepaymentHistory(loanId)
        );

        model.addAttribute("loanId", loanId);
        model.addAttribute("currentPage", "schedule");

        return "borrower/repayment-schedule";
    }

    // Mark installment as paid
    @PostMapping("/{repaymentId}/pay")
    public String markAsPaid(
            @PathVariable Long repaymentId,
            @RequestParam Long loanId,
            RedirectAttributes redirectAttributes) {

        try {
            repaymentService.markAsPaid(repaymentId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Installment paid successfully!"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/repayments/schedule?loanId=" + loanId;
    }

    // View repayment history
    @GetMapping("/history")
    public String showRepaymentHistory(
            @RequestParam Long loanId,
            Model model) {

        model.addAttribute(
                "repayments",
                repaymentService.getRepaymentHistory(loanId)
        );

        model.addAttribute("loanId", loanId);
        model.addAttribute("currentPage", "history");

        return "borrower/repayment-history";
    }
}
