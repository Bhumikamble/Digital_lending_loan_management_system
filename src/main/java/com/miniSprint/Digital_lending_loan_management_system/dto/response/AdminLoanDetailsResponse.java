package com.miniSprint.Digital_lending_loan_management_system.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AdminLoanDetailsResponse(

        AdminLoanResponse loan,

        long totalInstallments,
        long paidInstallments,
        long pendingInstallments,
        long overdueInstallments,

        BigDecimal totalRepayableAmount,
        BigDecimal totalPaidAmount,
        BigDecimal outstandingAmount,

        double repaymentProgress,

        List<AdminRepaymentResponse> repayments

) {
}
