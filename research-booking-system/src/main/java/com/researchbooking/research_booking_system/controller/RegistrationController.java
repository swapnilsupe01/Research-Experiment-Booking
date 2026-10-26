
package com.researchbooking.research_booking_system.controller;

import com.researchbooking.research_booking_system.entity.User;
import com.researchbooking.research_booking_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody Map<String, String> request) {

        String fullName = request.get("fullName");
        String email = request.get("email");
        String password = request.get("password");
        String confirmPassword = request.get("confirmPassword");

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Full name, email and password are required."));
        }

        // Email format validation
        if (!email.trim().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid email format."));
        }

        // Confirm password match check (if confirmPassword is provided)
        if (confirmPassword != null && !password.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password and confirm password must match."));
        }

        User user = new User();
        user.fullName = fullName.trim();
        user.email = email.trim();

        User savedUser;
        try {
            savedUser = userService.registerUser(user, password);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful! User ID: " + savedUser.userId);
        response.put("userId", savedUser.userId);
        response.put("fullName", savedUser.fullName);
        response.put("email", savedUser.email);
        response.put("role", savedUser.role);
        response.put("accountStatus", savedUser.accountStatus);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(java.security.Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required."));
        }
        User user = userService.getUserByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User account not found."));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.userId);
        data.put("fullName", user.fullName);
        data.put("email", user.email);
        data.put("role", user.role);
        data.put("accountStatus", user.accountStatus);
        return ResponseEntity.ok(data);
    }
}

