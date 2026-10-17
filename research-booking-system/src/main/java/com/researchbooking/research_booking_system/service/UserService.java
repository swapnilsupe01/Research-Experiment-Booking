
package com.researchbooking.research_booking_system.service;

import com.researchbooking.research_booking_system.entity.User;
import com.researchbooking.research_booking_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(User user, String rawPassword) {
        if (userRepository.existsByEmail(user.email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        user.passwordHash = passwordEncoder.encode(rawPassword);
        user.role = "STUDENT";
        user.accountStatus = "ACTIVE";

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found."));
    }
}
