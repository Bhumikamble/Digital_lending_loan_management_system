
package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.dto.BorrowerProfileRequest;
import com.miniSprint.Digital_lending_loan_management_system.dto.LoanApplicationRequest;
import com.miniSprint.Digital_lending_loan_management_system.entity.BorrowerProfile;
import com.miniSprint.Digital_lending_loan_management_system.entity.Loan;
import com.miniSprint.Digital_lending_loan_management_system.entity.User;
import com.miniSprint.Digital_lending_loan_management_system.repository.BorrowerProfileRepository;
import com.miniSprint.Digital_lending_loan_management_system.repository.UserRepository;
import com.miniSprint.Digital_lending_loan_management_system.service.BorrowerProfileService;
import com.miniSprint.Digital_lending_loan_management_system.service.LoanService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/borrower")
public class BorrowerWebController {

    private final UserRepository userRepository;
    private final BorrowerProfileRepository profileRepository;
    private final BorrowerProfileService profileService;
    private final LoanService loanService;

    public BorrowerWebController(
            UserRepository userRepository,
            BorrowerProfileRepository profileRepository,
            BorrowerProfileService profileService,
            LoanService loanService) {

        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.profileService = profileService;
        this.loanService = loanService;
    }

    // Get currently logged-in user
    private User getLoggedInUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Logged-in user not found"));
    }

    // 1. Borrower Dashboard
    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        User user = getLoggedInUser(authentication);

        Optional<BorrowerProfile> profile =
                profileRepository.findByUser_Id(user.getId());

        List<Loan> loans = profile.isPresent()
                ? loanService.getMyLoans(user.getId())
                : List.of();

        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("profile", profile.orElse(null));
        model.addAttribute("profileCompleted", profile.isPresent());
        model.addAttribute("loans", loans);
        model.addAttribute("totalLoans", loans.size());

        model.addAttribute("pendingLoans",
                loans.stream()
                        .filter(l -> l.getStatus().name().equals("APPLIED"))
                        .count());

        model.addAttribute("approvedLoans",
                loans.stream()
                        .filter(l -> l.getStatus().name().equals("APPROVED"))
                        .count());

        model.addAttribute("rejectedLoans",
                loans.stream()
                        .filter(l -> l.getStatus().name().equals("REJECTED"))
                        .count());

        return "borrower/dashboard";
    }

    // 2. View Borrower Profile
    @GetMapping("/profile")
    public String showProfile(
            Authentication authentication,
            Model model) {

        User user = getLoggedInUser(authentication);

        Optional<BorrowerProfile> profile =
                profileRepository.findByUser_Id(user.getId());

        BorrowerProfileRequest request =
                new BorrowerProfileRequest();

        if (profile.isPresent()) {

            BorrowerProfile p = profile.get();

            request.setFullName(p.getFullName());
            request.setPhone(p.getPhone());
            request.setDateOfBirth(p.getDateOfBirth());
            request.setAddress(p.getAddress());
            request.setEmploymentType(p.getEmploymentType());
            request.setMonthlyIncome(p.getMonthlyIncome());
        }

        model.addAttribute("profileRequest", request);
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("profileCompleted", profile.isPresent());

        return "borrower/profile";
    }

    // 3. Create or Update Borrower Profile
    @PostMapping("/profile")
    public String saveProfile(
            Authentication authentication,
            @Valid @ModelAttribute("profileRequest")
            BorrowerProfileRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        if (result.hasErrors()) {
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("profileCompleted",
                    profileRepository.existsByUser_Id(user.getId()));
            return "borrower/profile";
        }

        try {

            Optional<BorrowerProfile> existingProfile =
                    profileRepository.findByUser_Id(user.getId());

            if (existingProfile.isPresent()) {

                profileService.updateProfile(
                        user.getId(),
                        request.getFullName(),
                        request.getPhone(),
                        request.getDateOfBirth(),
                        request.getAddress(),
                        request.getEmploymentType(),
                        request.getMonthlyIncome()
                );

            } else {

                profileService.createProfile(
                        user.getId(),
                        request.getFullName(),
                        request.getPhone(),
                        request.getDateOfBirth(),
                        request.getAddress(),
                        request.getEmploymentType(),
                        request.getMonthlyIncome()
                );
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Profile saved successfully");

            return "redirect:/borrower/profile";

        } catch (RuntimeException ex) {

            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("profileCompleted",
                    profileRepository.existsByUser_Id(user.getId()));

            return "borrower/profile";
        }
    }

    // 4. Display Loan Application Form
    @GetMapping("/apply-loan")
    public String showLoanApplication(
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        if (!profileRepository.existsByUser_Id(user.getId())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please complete your borrower profile first");

            return "redirect:/borrower/profile";
        }

        model.addAttribute("loanRequest",
                new LoanApplicationRequest());

        model.addAttribute("userEmail", user.getEmail());

        return "borrower/loan-application";
    }

    // 5. Submit Loan Application
    @PostMapping("/apply-loan")
    public String applyForLoan(
            Authentication authentication,
            @Valid @ModelAttribute("loanRequest")
            LoanApplicationRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        if (result.hasErrors()) {
            model.addAttribute("userEmail", user.getEmail());
            return "borrower/loan-application";
        }

        try {

            loanService.applyForLoan(
                    user.getId(),
                    request.getAmount(),
                    request.getTenureMonths()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Loan application submitted successfully");

            return "redirect:/borrower/my-loans";

        } catch (RuntimeException ex) {

            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("errorMessage", ex.getMessage());

            return "borrower/loan-application";
        }
    }

    // 6. View My Loans
    @GetMapping("/my-loans")
    public String myLoans(
            Authentication authentication,
            Model model) {

        User user = getLoggedInUser(authentication);

        List<Loan> loans = loanService.getMyLoans(user.getId());

        model.addAttribute("loans", loans);
        model.addAttribute("userEmail", user.getEmail());

        return "borrower/my-loans";
    }

    // 7. View Individual Loan Details
    @GetMapping("/loans/{id}")
    public String loanDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        User user = getLoggedInUser(authentication);

        Loan loan = loanService.getMyLoanById(id, user.getId());

        model.addAttribute("loan", loan);
        model.addAttribute("userEmail", user.getEmail());

        return "borrower/loan-details";
    }
}
