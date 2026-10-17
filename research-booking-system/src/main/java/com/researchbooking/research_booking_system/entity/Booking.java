
package com.researchbooking.research_booking_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    public Long bookingId;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "slot_id", nullable = false)
    public Long slotId;

    @Column(name = "purpose", columnDefinition = "TEXT")
    public String purpose;

    @Column(name = "status", nullable = false)
    public String status = "PENDING";

    @Column(name = "requested_at", insertable = false, updatable = false)
    public LocalDateTime requestedAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    public LocalDateTime updatedAt;

    public Booking() {
    }
}
