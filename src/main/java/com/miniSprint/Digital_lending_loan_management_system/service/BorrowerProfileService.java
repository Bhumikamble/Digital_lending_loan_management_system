
package com.miniSprint.Digital_lending_loan_management_system.service;

import com.miniSprint.Digital_lending_loan_management_system.entity.BorrowerProfile;
import com.miniSprint.Digital_lending_loan_management_system.entity.User;
import com.miniSprint.Digital_lending_loan_management_system.repository.BorrowerProfileRepository;
import com.miniSprint.Digital_lending_loan_management_system.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class BorrowerProfileService {

    private final BorrowerProfileRepository borrowerProfileRepository;
    private final UserRepository userRepository;

    public BorrowerProfileService(
            BorrowerProfileRepository borrowerProfileRepository,
            UserRepository userRepository) {
        this.borrowerProfileRepository = borrowerProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BorrowerProfile createProfile(
            Long userId,
            String fullName,
            String phone,
            LocalDate dateOfBirth,
            String address,
            String employmentType,
            BigDecimal monthlyIncome) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (borrowerProfileRepository.existsByUser_Id(userId)) {
            throw new RuntimeException(
                    "Borrower profile already exists");
        }

        BorrowerProfile profile = new BorrowerProfile();

        profile.setUser(user);
        profile.setFullName(fullName);
        profile.setPhone(phone);
        profile.setDateOfBirth(dateOfBirth);
        profile.setAddress(address);
        profile.setEmploymentType(employmentType);
        profile.setMonthlyIncome(monthlyIncome);

        return borrowerProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public BorrowerProfile getProfileByUserId(Long userId) {

        return borrowerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Borrower profile not found"));
    }
}
