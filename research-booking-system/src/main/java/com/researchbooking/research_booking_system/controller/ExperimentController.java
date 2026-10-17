
package com.researchbooking.research_booking_system.controller;

import com.researchbooking.research_booking_system.entity.Experiment;
import com.researchbooking.research_booking_system.entity.ExperimentSlot;
import com.researchbooking.research_booking_system.service.ExperimentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping
    public List<Experiment> getActiveExperiments() {
        return experimentService.getActiveExperiments();
    }

    @GetMapping("/{experimentId}")
    public Experiment getExperiment(@PathVariable Long experimentId) {
        return experimentService.getExperimentById(experimentId);
    }

    @GetMapping("/{experimentId}/slots")
    public List<ExperimentSlot> getSlots(
            @PathVariable Long experimentId) {
        return experimentService.getSlotsForExperiment(experimentId);
    }

    @GetMapping("/slots/upcoming")
    public List<ExperimentSlot> getUpcomingSlots() {
        return experimentService.getUpcomingSlots();
    }
}
