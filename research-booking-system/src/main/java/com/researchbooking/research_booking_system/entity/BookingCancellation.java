
package com.researchbooking.research_booking_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_cancellations")
public class BookingCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancellation_id")
    public Long cancellationId;

    @Column(name = "booking_id", nullable = false, unique = true)
    public Long bookingId;

    @Column(name = "cancelled_by", nullable = false)
    public Long cancelledBy;

    @Column(name = "reason", length = 500)
    public String reason;

    @Column(name = "cancelled_at", insertable = false, updatable = false)
    public LocalDateTime cancelledAt;

    public BookingCancellation() {
    }
}
