
package com.miniSprint.Digital_lending_loan_management_system.controller;

import com.miniSprint.Digital_lending_loan_management_system.dto.BorrowerProfileRequest;
import com.miniSprint.Digital_lending_loan_management_system.dto.BorrowerProfileResponse;
import com.miniSprint.Digital_lending_loan_management_system.entity.BorrowerProfile;
import com.miniSprint.Digital_lending_loan_management_system.service.BorrowerProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrower/profile")
public class BorrowerProfileController {

    private final BorrowerProfileService borrowerProfileService;

    public BorrowerProfileController(
            BorrowerProfileService borrowerProfileService) {
        this.borrowerProfileService = borrowerProfileService;
    }

    // Create borrower profile
    @PostMapping
    public ResponseEntity<BorrowerProfileResponse> createProfile(
            @RequestParam Long userId,
            @Valid @RequestBody BorrowerProfileRequest request) {

        BorrowerProfile profile = borrowerProfileService.createProfile(
                userId,
                request.getFullName(),
                request.getPhone(),
                request.getDateOfBirth(),
                request.getAddress(),
                request.getEmploymentType(),
                request.getMonthlyIncome()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(profile));
    }

    // Get borrower profile
    @GetMapping
    public ResponseEntity<BorrowerProfileResponse> getProfile(
            @RequestParam Long userId) {

        BorrowerProfile profile =
                borrowerProfileService.getProfileByUserId(userId);

        return ResponseEntity.ok(convertToResponse(profile));
    }

    // Update borrower profile
    @PutMapping
    public ResponseEntity<BorrowerProfileResponse> updateProfile(
            @RequestParam Long userId,
            @Valid @RequestBody BorrowerProfileRequest request) {

        BorrowerProfile profile = borrowerProfileService.updateProfile(
                userId,
                request.getFullName(),
                request.getPhone(),
                request.getDateOfBirth(),
                request.getAddress(),
                request.getEmploymentType(),
                request.getMonthlyIncome()
        );

        return ResponseEntity.ok(convertToResponse(profile));
    }

    // Convert Entity into Response DTO
    private BorrowerProfileResponse convertToResponse(
            BorrowerProfile profile) {

        return new BorrowerProfileResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getUser().getEmail(),
                profile.getPhone(),
                profile.getDateOfBirth(),
                profile.getAddress(),
                profile.getEmploymentType(),
                profile.getMonthlyIncome()
        );
    }
}
