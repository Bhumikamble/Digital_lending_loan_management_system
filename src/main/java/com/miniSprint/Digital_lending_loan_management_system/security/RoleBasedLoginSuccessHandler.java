
package com.miniSprint.Digital_lending_loan_management_system.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedLoginSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String selectedRole = request.getParameter("role");

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        String actualRole = isAdmin ? "ADMIN" : "BORROWER";

        if (selectedRole == null || !selectedRole.equals(actualRole)) {

            SecurityContextHolder.clearContext();

            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }

            response.sendRedirect("/login?error");
            return;
        }

        if (isAdmin) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/borrower/dashboard");
        }
    }
}
