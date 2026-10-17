
package com.researchbooking.research_booking_system.service;

import com.researchbooking.research_booking_system.entity.Experiment;
import com.researchbooking.research_booking_system.entity.ExperimentSlot;
import com.researchbooking.research_booking_system.repository.ExperimentRepository;
import com.researchbooking.research_booking_system.repository.ExperimentSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final ExperimentSlotRepository slotRepository;

    public ExperimentService(
            ExperimentRepository experimentRepository,
            ExperimentSlotRepository slotRepository) {
        this.experimentRepository = experimentRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional(readOnly = true)
    public List<Experiment> getActiveExperiments() {
        return experimentRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Experiment getExperimentById(Long experimentId) {
        return experimentRepository.findById(experimentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Experiment not found."));
    }

    @Transactional(readOnly = true)
    public List<ExperimentSlot> getSlotsForExperiment(Long experimentId) {
        getExperimentById(experimentId);
        return slotRepository.findByExperimentId(experimentId)
                .stream()
                .filter(slot -> slot.startTime != null
                        && slot.startTime.isAfter(LocalDateTime.now()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExperimentSlot> getUpcomingSlots() {
        return slotRepository.findByStartTimeAfter(LocalDateTime.now());
    }
}
