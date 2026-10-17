
package com.researchbooking.research_booking_system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return """
                Research Experiment Booking System

                Application Status: Running
                Database: MySQL
                MySQL Port: 3307
                Application Port: 8081

                Welcome! Your application is running successfully.
                """;
    }
}
