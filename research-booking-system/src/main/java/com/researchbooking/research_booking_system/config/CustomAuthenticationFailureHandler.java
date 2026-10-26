package com.researchbooking.research_booking_system.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        boolean isDisabled = (exception instanceof DisabledException);
        String errorCode = isDisabled ? "disabled" : "invalid";
        String errorMessage = isDisabled
                ? "Your account is inactive. Please contact an administrator."
                : "Invalid email or password. Please try again.";

        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + errorMessage + "\",\"code\":\"" + errorCode + "\"}");
            return;
        }

        response.sendRedirect("/login?error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8));
    }
}
