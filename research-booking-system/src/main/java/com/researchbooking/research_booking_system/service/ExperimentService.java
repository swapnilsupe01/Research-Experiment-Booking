
package com.researchbooking.research_booking_system.service;

import com.researchbooking.research_booking_system.dto.ExperimentRequest;
import com.researchbooking.research_booking_system.dto.SlotRequest;
import com.researchbooking.research_booking_system.entity.Booking;
import com.researchbooking.research_booking_system.entity.Experiment;
import com.researchbooking.research_booking_system.entity.ExperimentSlot;
import com.researchbooking.research_booking_system.repository.BookingRepository;
import com.researchbooking.research_booking_system.repository.ExperimentRepository;
import com.researchbooking.research_booking_system.repository.ExperimentSlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final ExperimentSlotRepository slotRepository;
    private final BookingRepository bookingRepository;

    public ExperimentService(
            ExperimentRepository experimentRepository,
            ExperimentSlotRepository slotRepository,
            BookingRepository bookingRepository) {
        this.experimentRepository = experimentRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
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
        // Return all slots for this experiment, sorted by start time.
        // The teacher is responsible for scheduling slots with appropriate future dates.
        return slotRepository.findByExperimentId(experimentId)
                .stream()
                .filter(slot -> slot.startTime != null)
                .sorted(java.util.Comparator.comparing(slot -> slot.startTime))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExperimentSlot> getUpcomingSlots() {
        return slotRepository.findByStartTimeAfter(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Experiment> getExperimentsBySupervisor(Long supervisorId) {
        return experimentRepository.findBySupervisorId(supervisorId);
    }

    @Transactional
    public Experiment createExperiment(ExperimentRequest request, Long supervisorId) {
        validateExperimentData(request);

        Experiment experiment = new Experiment();
        experiment.title = request.title().trim();
        experiment.description = request.description();
        experiment.laboratoryName = request.laboratoryName().trim();
        experiment.durationMinutes = request.durationMinutes();
        experiment.supervisorId = supervisorId;
        experiment.active = true;

        return experimentRepository.save(experiment);
    }

    @Transactional
    public Experiment updateExperiment(Long experimentId, ExperimentRequest request, Long supervisorId) {
        Experiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiment not found."));

        if (!java.util.Objects.equals(experiment.supervisorId, supervisorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this experiment.");
        }

        validateExperimentData(request);

        experiment.title = request.title().trim();
        experiment.description = request.description();
        experiment.laboratoryName = request.laboratoryName().trim();
        experiment.durationMinutes = request.durationMinutes();

        return experimentRepository.save(experiment);
    }

    @Transactional
    public void deactivateExperiment(Long experimentId, Long supervisorId) {
        Experiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiment not found."));

        if (!java.util.Objects.equals(experiment.supervisorId, supervisorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to deactivate this experiment.");
        }

        experiment.active = false;
        experimentRepository.save(experiment);
    }

    @Transactional
    public void deleteExperimentPermanently(Long experimentId, Long supervisorId) {
        Experiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiment not found."));

        if (!java.util.Objects.equals(experiment.supervisorId, supervisorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this experiment.");
        }

        // Safety check: block deletion if any active bookings exist on any slot
        List<ExperimentSlot> slots = slotRepository.findByExperimentId(experimentId);
        for (ExperimentSlot slot : slots) {
            List<com.researchbooking.research_booking_system.entity.Booking> bookings =
                    bookingRepository.findBySlotId(slot.slotId);
            boolean hasActive = bookings.stream()
                    .anyMatch(b -> "CONFIRMED".equalsIgnoreCase(b.status) || "PENDING".equalsIgnoreCase(b.status));
            if (hasActive) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot delete experiment '" + experiment.title +
                        "' because it has active (PENDING or CONFIRMED) bookings. Cancel or reject those bookings first.");
            }
        }

        // Delete all slots first (FK constraint), then the experiment
        slotRepository.deleteAll(slots);
        experimentRepository.delete(experiment);
    }


    private void validateExperimentData(ExperimentRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Experiment title is required.");
        }
        if (request.title().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Experiment title cannot exceed 150 characters.");
        }
        if (request.laboratoryName() == null || request.laboratoryName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Laboratory name is required.");
        }
        if (request.laboratoryName().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Laboratory name cannot exceed 150 characters.");
        }
        if (request.durationMinutes() == null || request.durationMinutes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be greater than zero.");
        }
    }

    @Transactional(readOnly = true)
    public List<ExperimentSlot> getSlotsForTeacher(Long experimentId, Long teacherId) {
        Experiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiment not found."));

        if (!java.util.Objects.equals(experiment.supervisorId, teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view slots for this experiment.");
        }

        return slotRepository.findByExperimentId(experimentId);
    }

    @Transactional
    public ExperimentSlot createSlot(Long experimentId, SlotRequest request, Long teacherId) {
        validateSlotData(request);

        Experiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiment not found."));

        if (!java.util.Objects.equals(experiment.supervisorId, teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to manage slots for this experiment.");
        }

        if (Boolean.FALSE.equals(experiment.active)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add slots to an inactive experiment.");
        }

        ExperimentSlot slot = new ExperimentSlot();
        slot.experimentId = experimentId;
        slot.startTime = request.startTime();
        slot.endTime = request.endTime();
        slot.capacity = request.capacity();

        return slotRepository.save(slot);
    }

    @Transactional
    public ExperimentSlot updateSlot(Long slotId, SlotRequest request, Long teacherId) {
        validateSlotData(request);

        ExperimentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found."));

        Experiment experiment = experimentRepository.findById(slot.experimentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiment not found."));

        if (!java.util.Objects.equals(experiment.supervisorId, teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to manage slots for this experiment.");
        }

        if (Boolean.FALSE.equals(experiment.active)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot modify slots for an inactive experiment.");
        }

        List<Booking> bookings = bookingRepository.findBySlotId(slotId);

        long confirmedCount = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.status))
                .count();

        if (request.capacity() < confirmedCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Capacity cannot be less than confirmed bookings count (" + confirmedCount + ").");
        }

        boolean timingChanged = !java.util.Objects.equals(slot.startTime, request.startTime())
                || !java.util.Objects.equals(slot.endTime, request.endTime());
        if (timingChanged) {
            boolean hasActiveBookings = bookings.stream()
                    .anyMatch(b -> "CONFIRMED".equalsIgnoreCase(b.status) || "PENDING".equalsIgnoreCase(b.status));
            if (hasActiveBookings) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot change slot timing because active bookings exist.");
            }
        }

        slot.startTime = request.startTime();
        slot.endTime = request.endTime();
        slot.capacity = request.capacity();

        return slotRepository.save(slot);
    }

    @Transactional
    public void deleteSlot(Long slotId, Long teacherId) {
        ExperimentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found."));

        Experiment experiment = experimentRepository.findById(slot.experimentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiment not found."));

        if (!java.util.Objects.equals(experiment.supervisorId, teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete slots for this experiment.");
        }

        List<Booking> bookings = bookingRepository.findBySlotId(slotId);
        boolean hasActiveBookings = bookings.stream()
                .anyMatch(b -> "CONFIRMED".equalsIgnoreCase(b.status) || "PENDING".equalsIgnoreCase(b.status));
        if (hasActiveBookings) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete slot with active bookings.");
        }
        if (!bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete slot that has existing booking records.");
        }

        slotRepository.delete(slot);
    }

    private void validateSlotData(SlotRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }
        if (request.startTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time is required.");
        }
        if (request.endTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time is required.");
        }
        if (!request.startTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot start time must be in the future.");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot end time must be after start time.");
        }
        if (request.capacity() == null || request.capacity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capacity must be greater than zero.");
        }
    }
}

