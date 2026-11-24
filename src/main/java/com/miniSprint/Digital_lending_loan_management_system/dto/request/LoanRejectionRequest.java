package com.miniSprint.Digital_lending_loan_management_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanRejectionRequest {

    @NotBlank(message = "Rejection remark is required")
    @Size(max = 500, message = "Remark cannot exceed 500 characters")
    private String rejectionRemark;
}
