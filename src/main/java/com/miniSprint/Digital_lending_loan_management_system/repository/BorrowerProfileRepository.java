
package com.miniSprint.Digital_lending_loan_management_system.repository;

import com.miniSprint.Digital_lending_loan_management_system.entity.BorrowerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowerProfileRepository
        extends JpaRepository<BorrowerProfile, Long> {

    // Find borrower profile using User ID
    Optional<BorrowerProfile> findByUser_Id(Long userId);

    // Check whether a profile already exists for a user
    boolean existsByUser_Id(Long userId);
}
