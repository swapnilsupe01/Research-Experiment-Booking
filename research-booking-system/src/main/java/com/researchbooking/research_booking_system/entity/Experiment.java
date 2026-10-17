
package com.researchbooking.research_booking_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "experiments")
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "experiment_id")
    public Long experimentId;

    @Column(name = "title", nullable = false, length = 150)
    public String title;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "laboratory_name", nullable = false, length = 150)
    public String laboratoryName;

    @Column(name = "supervisor_id", nullable = false)
    public Long supervisorId;

    @Column(name = "duration_minutes", nullable = false)
    public Integer durationMinutes;

    @Column(name = "is_active", nullable = false)
    public Boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    public LocalDateTime createdAt;

    public Experiment() {
    }
}
