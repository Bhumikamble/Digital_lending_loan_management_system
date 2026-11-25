
package com.miniSprint.Digital_lending_loan_management_system.repository;

import com.miniSprint.Digital_lending_loan_management_system.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface RepaymentRepository
        extends JpaRepository<Repayment, Long> {

    List<Repayment> findByLoanIdOrderByInstallmentNoAsc(Long loanId);

    boolean existsByLoanId(Long loanId);
    Optional<Repayment> findByIdAndLoan_Borrower_User_Id(
            Long repaymentId, Long userId);
}
