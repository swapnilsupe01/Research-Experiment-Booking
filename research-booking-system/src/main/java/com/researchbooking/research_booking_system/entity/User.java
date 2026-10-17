
package com.researchbooking.research_booking_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    public Long userId;

    @Column(name = "full_name", nullable = false, length = 100)
    public String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    public String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    public String passwordHash;

    @Column(name = "role", nullable = false)
    public String role;

    @Column(name = "account_status", nullable = false)
    public String accountStatus;

    @Column(name = "created_at", insertable = false, updatable = false)
    public LocalDateTime createdAt;

    public User() {
    }
}
