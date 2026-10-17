
package com.researchbooking.research_booking_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_status_history")
public class BookingStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    public Long historyId;

    @Column(name = "booking_id", nullable = false)
    public Long bookingId;

    @Column(name = "old_status", length = 20)
    public String oldStatus;

    @Column(name = "new_status", nullable = false, length = 20)
    public String newStatus;

    @Column(name = "changed_by", nullable = false)
    public Long changedBy;

    @Column(name = "remarks", length = 500)
    public String remarks;

    @Column(name = "changed_at", insertable = false, updatable = false)
    public LocalDateTime changedAt;

    public BookingStatusHistory() {
    }
}
