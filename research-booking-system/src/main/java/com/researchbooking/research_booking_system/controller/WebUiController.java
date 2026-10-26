
package com.researchbooking.research_booking_system.controller;

import com.researchbooking.research_booking_system.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class WebUiController {

    private final UserRepository userRepository;

    public WebUiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping({"/", "/ui", "/login"})
    public String loginOrIndex(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return getDashboardRedirect(authentication);
        }
        return "auth-login";
    }

    @GetMapping({"/landing", "/explore"})
    public String publicLanding() {
        return "index";
    }

    @GetMapping({"/signup", "/register"})
    public String signup(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return getDashboardRedirect(authentication);
        }
        return "auth-signup";
    }

    @GetMapping("/dashboard/student")
    public String studentDashboard(Model model, Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            model.addAttribute("currentUserEmail", email);
            model.addAttribute("currentUserRole", "Student");
            userRepository.findByEmail(email).ifPresent(user -> {
                model.addAttribute("currentUserId", user.userId);
                model.addAttribute("currentUserName", user.fullName);
            });
        }
        return "student-dashboard";
    }

    @GetMapping("/dashboard/teacher")
    public String teacherDashboard(Model model, Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            model.addAttribute("currentUserEmail", email);
            model.addAttribute("currentUserRole", "Teacher");
            userRepository.findByEmail(email).ifPresent(user -> {
                model.addAttribute("currentUserId", user.userId);
                model.addAttribute("currentUserName", user.fullName);
            });
        }
        return "teacher-dashboard";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String getDashboardRedirect(Authentication authentication) {
        boolean isTeacher = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_TEACHER".equalsIgnoreCase(a.getAuthority()));
        return isTeacher ? "redirect:/dashboard/teacher" : "redirect:/dashboard/student";
    }
}
