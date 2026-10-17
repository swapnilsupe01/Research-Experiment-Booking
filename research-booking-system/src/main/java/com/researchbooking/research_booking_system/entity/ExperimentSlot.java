
package com.researchbooking.research_booking_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "experiment_slots")
public class ExperimentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    public Long slotId;

    @Column(name = "experiment_id", nullable = false)
    public Long experimentId;

    @Column(name = "start_time", nullable = false)
    public LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    public LocalDateTime endTime;

    @Column(name = "capacity", nullable = false)
    public Integer capacity;

    @Column(name = "created_at", insertable = false, updatable = false)
    public LocalDateTime createdAt;

    public ExperimentSlot() {
    }
}
