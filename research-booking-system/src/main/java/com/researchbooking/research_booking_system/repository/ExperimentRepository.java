
package com.researchbooking.research_booking_system.repository;

import com.researchbooking.research_booking_system.entity.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    List<Experiment> findByActiveTrue();
}
