package com.researchbooking.research_booking_system.service;

import com.researchbooking.research_booking_system.dto.ExperimentRequest;
import com.researchbooking.research_booking_system.dto.SlotRequest;
import com.researchbooking.research_booking_system.entity.Booking;
import com.researchbooking.research_booking_system.entity.Experiment;
import com.researchbooking.research_booking_system.entity.ExperimentSlot;
import com.researchbooking.research_booking_system.repository.BookingRepository;
import com.researchbooking.research_booking_system.repository.ExperimentRepository;
import com.researchbooking.research_booking_system.repository.ExperimentSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentServiceTest {

    private Experiment experiment;
    private ExperimentSlot slot;
    private List<Booking> slotBookings;
    private AtomicBoolean saveCalled;
    private AtomicBoolean slotSaveCalled;
    private AtomicBoolean slotDeleteCalled;
    private ExperimentService experimentService;

    @BeforeEach
    void setUp() {
        experiment = new Experiment();
        experiment.experimentId = 1L;
        experiment.title = "Original Title";
        experiment.description = "Original Description";
        experiment.laboratoryName = "Lab A";
        experiment.durationMinutes = 60;
        experiment.supervisorId = 100L;
        experiment.active = true;

        slot = new ExperimentSlot();
        slot.slotId = 10L;
        slot.experimentId = 1L;
        slot.startTime = LocalDateTime.now().plusDays(1);
        slot.endTime = LocalDateTime.now().plusDays(1).plusHours(2);
        slot.capacity = 5;

        slotBookings = new ArrayList<>();
        saveCalled = new AtomicBoolean(false);
        slotSaveCalled = new AtomicBoolean(false);
        slotDeleteCalled = new AtomicBoolean(false);

        ClassLoader classLoader = getClass().getClassLoader();

        ExperimentRepository repoProxy = (ExperimentRepository) Proxy.newProxyInstance(
                classLoader,
                new Class<?>[]{ExperimentRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(experiment);
                    }
                    if ("save".equals(name)) {
                        saveCalled.set(true);
                        return args[0];
                    }
                    return null;
                }
        );

        ExperimentSlotRepository slotRepoProxy = (ExperimentSlotRepository) Proxy.newProxyInstance(
                classLoader,
                new Class<?>[]{ExperimentSlotRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(slot);
                    }
                    if ("save".equals(name)) {
                        slotSaveCalled.set(true);
                        return args[0];
                    }
                    if ("delete".equals(name)) {
                        slotDeleteCalled.set(true);
                        return null;
                    }
                    if ("findByExperimentId".equals(name)) {
                        return List.of(slot);
                    }
                    return null;
                }
        );

        BookingRepository bookingRepoProxy = (BookingRepository) Proxy.newProxyInstance(
                classLoader,
                new Class<?>[]{BookingRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findBySlotId".equals(name)) {
                        return slotBookings;
                    }
                    return null;
                }
        );

        experimentService = new ExperimentService(repoProxy, slotRepoProxy, bookingRepoProxy);
    }

    @Test
    void updateExperiment_whenSupervisorIdMismatch_throwsForbidden() {
        experiment.supervisorId = 100L;
        ExperimentRequest request = new ExperimentRequest("Updated Title", "Desc", "Lab B", 45);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.updateExperiment(1L, request, 200L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You do not have permission to modify this experiment.", exception.getReason());
        assertFalse(saveCalled.get(), "Save should not be called when permission is denied");
    }

    @Test
    void updateExperiment_whenExperimentSupervisorIdNull_throwsForbiddenWithoutNpe() {
        experiment.supervisorId = null;
        ExperimentRequest request = new ExperimentRequest("Updated Title", "Desc", "Lab B", 45);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.updateExperiment(1L, request, 200L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You do not have permission to modify this experiment.", exception.getReason());
        assertFalse(saveCalled.get(), "Save should not be called when supervisorId is null");
    }

    @Test
    void deactivateExperiment_whenSupervisorIdMismatch_throwsForbidden() {
        experiment.supervisorId = 100L;

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.deactivateExperiment(1L, 200L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You do not have permission to deactivate this experiment.", exception.getReason());
        assertFalse(saveCalled.get(), "Save should not be called when permission is denied");
    }

    @Test
    void deactivateExperiment_whenExperimentSupervisorIdNull_throwsForbiddenWithoutNpe() {
        experiment.supervisorId = null;

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.deactivateExperiment(1L, 200L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You do not have permission to deactivate this experiment.", exception.getReason());
        assertFalse(saveCalled.get(), "Save should not be called when supervisorId is null");
    }

    @Test
    void deactivateExperiment_whenOwnerMatches_deactivatesSuccessfully() {
        experiment.supervisorId = 100L;

        experimentService.deactivateExperiment(1L, 100L);

        assertFalse(experiment.active);
        assertTrue(saveCalled.get(), "Save should be called when ownership matches");
    }

    // --- Slot Management Tests ---

    @Test
    void createSlot_whenTeacherOwnsExperiment_succeeds() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(2);
        SlotRequest request = new SlotRequest(start, end, 8);

        ExperimentSlot created = experimentService.createSlot(1L, request, 100L);

        assertNotNull(created);
        assertEquals(1L, created.experimentId);
        assertEquals(start, created.startTime);
        assertEquals(end, created.endTime);
        assertEquals(8, created.capacity);
        assertTrue(slotSaveCalled.get());
    }

    @Test
    void createSlot_whenTeacherDoesNotOwnExperiment_throwsForbidden() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(2);
        SlotRequest request = new SlotRequest(start, end, 8);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.createSlot(1L, request, 999L)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertFalse(slotSaveCalled.get());
    }

    @Test
    void createSlot_whenExperimentInactive_throwsBadRequest() {
        experiment.active = false;
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(2);
        SlotRequest request = new SlotRequest(start, end, 8);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.createSlot(1L, request, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("inactive"));
    }

    @Test
    void createSlot_withPastStartTime_throwsBadRequest() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        SlotRequest request = new SlotRequest(start, end, 5);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.createSlot(1L, request, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("future"));
    }

    @Test
    void createSlot_withEndTimeBeforeStartTime_throwsBadRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.minusHours(1);
        SlotRequest request = new SlotRequest(start, end, 5);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.createSlot(1L, request, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("after start time"));
    }

    @Test
    void createSlot_withNonPositiveCapacity_throwsBadRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        SlotRequest request = new SlotRequest(start, end, 0);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.createSlot(1L, request, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Capacity must be greater than zero"));
    }

    @Test
    void updateSlot_whenTeacherOwnsExperiment_succeeds() {
        LocalDateTime newStart = slot.startTime;
        LocalDateTime newEnd = slot.endTime;
        SlotRequest request = new SlotRequest(newStart, newEnd, 10);

        ExperimentSlot updated = experimentService.updateSlot(10L, request, 100L);

        assertEquals(10, updated.capacity);
        assertTrue(slotSaveCalled.get());
    }

    @Test
    void updateSlot_whenExperimentInactive_throwsBadRequest() {
        experiment.active = false;
        SlotRequest request = new SlotRequest(slot.startTime, slot.endTime, 10);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.updateSlot(10L, request, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("inactive"));
        assertFalse(slotSaveCalled.get());
    }

    @Test
    void updateSlot_whenSlotStartTimeOrEndTimeNull_detectsChangeWithoutNpe() {
        slot.startTime = null;
        slot.endTime = null;
        LocalDateTime newStart = LocalDateTime.now().plusDays(2);
        LocalDateTime newEnd = newStart.plusHours(2);
        SlotRequest request = new SlotRequest(newStart, newEnd, 10);

        ExperimentSlot updated = experimentService.updateSlot(10L, request, 100L);

        assertNotNull(updated);
        assertEquals(newStart, updated.startTime);
        assertEquals(newEnd, updated.endTime);
        assertTrue(slotSaveCalled.get());
    }

    @Test
    void updateSlot_whenTimingUnchangedAndActiveBookingsExist_succeeds() {
        Booking b = new Booking();
        b.status = "CONFIRMED";
        slotBookings.add(b);

        SlotRequest request = new SlotRequest(slot.startTime, slot.endTime, 10);

        ExperimentSlot updated = experimentService.updateSlot(10L, request, 100L);

        assertNotNull(updated);
        assertEquals(10, updated.capacity);
        assertTrue(slotSaveCalled.get());
    }

    @Test
    void updateSlot_whenTeacherDoesNotOwnExperiment_throwsForbidden() {
        SlotRequest request = new SlotRequest(slot.startTime, slot.endTime, 10);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.updateSlot(10L, request, 999L)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertFalse(slotSaveCalled.get());
    }

    @Test
    void updateSlot_whenCapacityLessThanConfirmedBookings_throwsBadRequest() {
        Booking b1 = new Booking();
        b1.status = "CONFIRMED";
        Booking b2 = new Booking();
        b2.status = "CONFIRMED";
        slotBookings.add(b1);
        slotBookings.add(b2);

        SlotRequest request = new SlotRequest(slot.startTime, slot.endTime, 1);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.updateSlot(10L, request, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Capacity cannot be less than confirmed bookings"));
        assertFalse(slotSaveCalled.get());
    }

    @Test
    void updateSlot_whenTimeShiftWithActiveBookings_throwsBadRequest() {
        Booking b = new Booking();
        b.status = "PENDING";
        slotBookings.add(b);

        LocalDateTime shiftedStart = slot.startTime.plusHours(1);
        LocalDateTime shiftedEnd = slot.endTime.plusHours(1);
        SlotRequest request = new SlotRequest(shiftedStart, shiftedEnd, slot.capacity);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.updateSlot(10L, request, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("active bookings exist"));
        assertFalse(slotSaveCalled.get());
    }

    @Test
    void deleteSlot_whenTeacherOwnsExperimentAndNoBookings_succeeds() {
        experimentService.deleteSlot(10L, 100L);

        assertTrue(slotDeleteCalled.get());
    }

    @Test
    void deleteSlot_whenTeacherDoesNotOwnExperiment_throwsForbidden() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.deleteSlot(10L, 999L)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertFalse(slotDeleteCalled.get());
    }

    @Test
    void deleteSlot_whenActiveBookingsExist_throwsBadRequest() {
        Booking b = new Booking();
        b.status = "CONFIRMED";
        slotBookings.add(b);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.deleteSlot(10L, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("active bookings"));
        assertFalse(slotDeleteCalled.get());
    }

    @Test
    void deleteSlot_whenHistoricalBookingsExist_throwsBadRequest() {
        Booking b = new Booking();
        b.status = "CANCELLED";
        slotBookings.add(b);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.deleteSlot(10L, 100L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("existing booking records"));
        assertFalse(slotDeleteCalled.get());
    }

    @Test
    void getSlotsForTeacher_whenTeacherOwnsExperiment_returnsSlots() {
        List<ExperimentSlot> slots = experimentService.getSlotsForTeacher(1L, 100L);

        assertNotNull(slots);
        assertEquals(1, slots.size());
    }

    @Test
    void getSlotsForTeacher_whenTeacherDoesNotOwnExperiment_throwsForbidden() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> experimentService.getSlotsForTeacher(1L, 999L)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
