package com.miniSprint.Digital_lending_loan_management_system.service;
import java.util.List;
import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminDashboardResponse;
import com.miniSprint.Digital_lending_loan_management_system.dto.response.AdminLoanResponse;

public interface AdminService {

    AdminDashboardResponse getDashboardStats();

    List<AdminLoanResponse> getAllLoans();

    List<AdminLoanResponse> getLoansByStatus(String status);

    AdminLoanResponse approveLoan(Long loanId);

    AdminLoanResponse rejectLoan(Long loanId, String rejectionRemark);
}
