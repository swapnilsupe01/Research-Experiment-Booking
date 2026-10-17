
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

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Full name, email and password are required."));
        }

        User user = new User();
        user.fullName = fullName.trim();
        user.email = email.trim();

        User savedUser = userService.registerUser(user, password);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful.");
        response.put("userId", savedUser.userId);
        response.put("fullName", savedUser.fullName);
        response.put("email", savedUser.email);
        response.put("role", savedUser.role);
        response.put("accountStatus", savedUser.accountStatus);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
