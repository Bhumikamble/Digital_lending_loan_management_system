package com.miniSprint.Digital_lending_loan_management_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminRepaymentResponse(

        Long repaymentId,
        int installmentNo,
        LocalDate dueDate,
        BigDecimal principalAmount,
        BigDecimal interestAmount,
        BigDecimal amount,
        BigDecimal paidAmount,
        String status,
        LocalDateTime paidAt,
        boolean overdue

) {
}
