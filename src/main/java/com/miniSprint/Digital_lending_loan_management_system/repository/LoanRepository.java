
package com.miniSprint.Digital_lending_loan_management_system.repository;

import com.miniSprint.Digital_lending_loan_management_system.entity.Loan;
import com.miniSprint.Digital_lending_loan_management_system.entity.LoanStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Find all loans belonging to a borrower
    List<Loan> findByBorrower_Id(Long borrowerId);

    // Find a particular borrower's loan using User ID
    List<Loan> findByBorrower_User_Id(Long userId);

    // Find loans based on application status
    List<Loan> findByStatus(LoanStatus status);

    // Find a loan belonging to a particular borrower
    Optional<Loan> findByIdAndBorrower_User_Id(
            Long loanId,
            Long userId
    );

    // Check whether a borrower has a loan with a particular status
    boolean existsByBorrower_IdAndStatus(
            Long borrowerId,
            LoanStatus status
    );
}
