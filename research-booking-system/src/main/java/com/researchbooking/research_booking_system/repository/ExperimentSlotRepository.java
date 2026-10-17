
package com.researchbooking.research_booking_system.repository;

import com.researchbooking.research_booking_system.entity.ExperimentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ExperimentSlotRepository extends JpaRepository<ExperimentSlot, Long> {
    List<ExperimentSlot> findByExperimentId(Long experimentId);
    List<ExperimentSlot> findByStartTimeAfter(LocalDateTime currentTime);
}
