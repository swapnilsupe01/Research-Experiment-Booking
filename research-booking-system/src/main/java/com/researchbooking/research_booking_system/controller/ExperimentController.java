
package com.researchbooking.research_booking_system.controller;

import com.researchbooking.research_booking_system.dto.ExperimentRequest;
import com.researchbooking.research_booking_system.dto.SlotRequest;
import com.researchbooking.research_booking_system.entity.Experiment;
import com.researchbooking.research_booking_system.entity.ExperimentSlot;
import com.researchbooking.research_booking_system.entity.User;
import com.researchbooking.research_booking_system.repository.UserRepository;
import com.researchbooking.research_booking_system.service.ExperimentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;
    private final UserRepository userRepository;

    public ExperimentController(
            ExperimentService experimentService,
            UserRepository userRepository) {
        this.experimentService = experimentService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Experiment> getActiveExperiments() {
        return experimentService.getActiveExperiments();
    }

    @GetMapping("/supervisor/me")
    public ResponseEntity<List<Experiment>> getMyExperiments(Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        return ResponseEntity.ok(experimentService.getExperimentsBySupervisor(teacher.userId));
    }

    @GetMapping("/slots/upcoming")
    public List<ExperimentSlot> getUpcomingSlots() {
        return experimentService.getUpcomingSlots();
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

    @PostMapping
    public ResponseEntity<Experiment> createExperiment(
            @RequestBody ExperimentRequest request,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        Experiment created = experimentService.createExperiment(request, teacher.userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{experimentId}")
    public ResponseEntity<Experiment> updateExperiment(
            @PathVariable Long experimentId,
            @RequestBody ExperimentRequest request,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        Experiment updated = experimentService.updateExperiment(experimentId, request, teacher.userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{experimentId}")
    public ResponseEntity<Void> deactivateExperiment(
            @PathVariable Long experimentId,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        experimentService.deactivateExperiment(experimentId, teacher.userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{experimentId}/permanent")
    public ResponseEntity<Void> deleteExperimentPermanently(
            @PathVariable Long experimentId,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        experimentService.deleteExperimentPermanently(experimentId, teacher.userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supervisor/{experimentId}/slots")
    public ResponseEntity<List<ExperimentSlot>> getTeacherSlots(
            @PathVariable Long experimentId,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        return ResponseEntity.ok(experimentService.getSlotsForTeacher(experimentId, teacher.userId));
    }

    @PostMapping("/{experimentId}/slots")
    public ResponseEntity<ExperimentSlot> createSlot(
            @PathVariable Long experimentId,
            @RequestBody SlotRequest request,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        ExperimentSlot created = experimentService.createSlot(experimentId, request, teacher.userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/slots/{slotId}")
    public ResponseEntity<ExperimentSlot> updateSlot(
            @PathVariable Long slotId,
            @RequestBody SlotRequest request,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        ExperimentSlot updated = experimentService.updateSlot(slotId, request, teacher.userId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{experimentId}/slots/{slotId}")
    public ResponseEntity<ExperimentSlot> updateExperimentSlot(
            @PathVariable Long experimentId,
            @PathVariable Long slotId,
            @RequestBody SlotRequest request,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        ExperimentSlot updated = experimentService.updateSlot(slotId, request, teacher.userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long slotId,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        experimentService.deleteSlot(slotId, teacher.userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{experimentId}/slots/{slotId}")
    public ResponseEntity<Void> deleteExperimentSlot(
            @PathVariable Long experimentId,
            @PathVariable Long slotId,
            Principal principal) {
        User teacher = getAuthenticatedTeacher(principal);
        experimentService.deleteSlot(slotId, teacher.userId);
        return ResponseEntity.noContent().build();
    }


    private User getAuthenticatedTeacher(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user account not found."));

        boolean isTeacherRole = "TEACHER".equalsIgnoreCase(user.role) || "RESEARCHER".equalsIgnoreCase(user.role);
        if (!isTeacherRole || user.accountStatus == null || !user.accountStatus.equalsIgnoreCase("ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Active teacher account required.");
        }

        return user;
    }
}
