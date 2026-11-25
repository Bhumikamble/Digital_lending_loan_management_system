package com.miniSprint.Digital_lending_loan_management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {

    private long totalApplications;
    private long pendingApplications;
    private long approvedLoans;
    private long rejectedLoans;
    private long closedLoans;
}
