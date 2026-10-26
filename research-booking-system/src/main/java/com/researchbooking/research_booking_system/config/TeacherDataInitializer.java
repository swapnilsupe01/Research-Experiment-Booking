package com.researchbooking.research_booking_system.config;

import com.researchbooking.research_booking_system.entity.User;
import com.researchbooking.research_booking_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TeacherDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Ensure default faculty/supervisor account exists for lab allotment demonstration
        if (userRepository.findByEmail("teacher@university.edu").isEmpty()) {
            User teacher = new User();
            teacher.fullName = "Dr. Alan Vance (Faculty Supervisor)";
            teacher.email = "teacher@university.edu";
            teacher.passwordHash = passwordEncoder.encode("TeacherPass123!");
            teacher.role = "RESEARCHER";
            teacher.accountStatus = "ACTIVE";
            userRepository.save(teacher);
        }
    }
}
