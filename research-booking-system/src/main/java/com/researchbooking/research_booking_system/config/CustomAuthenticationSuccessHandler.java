package com.researchbooking.research_booking_system.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isTeacher = authorities.stream()
                .anyMatch(a -> "ROLE_TEACHER".equalsIgnoreCase(a.getAuthority()));

        String portal = request.getParameter("portal");

        // Verify role match if user specifically selected a portal entry point
        if ("teacher".equalsIgnoreCase(portal) && !isTeacher) {
            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            String accept = request.getHeader("Accept");
            if (accept != null && accept.contains("application/json")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Access denied: Account is not authorized for the Teacher Portal.\",\"code\":\"role_mismatch_teacher\"}");
                return;
            }
            response.sendRedirect("/login?error=role_mismatch_teacher&portal=teacher");
            return;
        }

        if ("student".equalsIgnoreCase(portal) && isTeacher) {
            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            String accept = request.getHeader("Accept");
            if (accept != null && accept.contains("application/json")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Access denied: Teacher account cannot log in via the Student Portal.\",\"code\":\"role_mismatch_student\"}");
                return;
            }
            response.sendRedirect("/login?error=role_mismatch_student&portal=student");
            return;
        }

        String redirectUrl = isTeacher ? "/dashboard/teacher" : "/dashboard/student";

        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true,\"role\":\"" + (isTeacher ? "TEACHER" : "STUDENT") + "\",\"redirectUrl\":\"" + redirectUrl + "\"}");
            return;
        }

        response.sendRedirect(redirectUrl);
    }
}
