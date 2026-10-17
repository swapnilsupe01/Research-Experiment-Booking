
package com.researchbooking.research_booking_system.service;

import com.researchbooking.research_booking_system.entity.Booking;
import com.researchbooking.research_booking_system.entity.BookingCancellation;
import com.researchbooking.research_booking_system.entity.BookingStatusHistory;
import com.researchbooking.research_booking_system.entity.ExperimentSlot;
import com.researchbooking.research_booking_system.repository.BookingCancellationRepository;
import com.researchbooking.research_booking_system.repository.BookingRepository;
import com.researchbooking.research_booking_system.repository.BookingStatusHistoryRepository;
import com.researchbooking.research_booking_system.repository.ExperimentSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ExperimentSlotRepository slotRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final BookingCancellationRepository cancellationRepository;

    public BookingService(
            BookingRepository bookingRepository,
            ExperimentSlotRepository slotRepository,
            BookingStatusHistoryRepository historyRepository,
            BookingCancellationRepository cancellationRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.historyRepository = historyRepository;
        this.cancellationRepository = cancellationRepository;
    }

    @Transactional
    public Booking requestBooking(Long userId, Long slotId, String purpose) {
        ExperimentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Time slot not found."));

        if (slot.startTime == null ||
                !slot.startTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Bookings are only allowed for future time slots.");
        }

        long occupied = bookingRepository.findBySlotId(slotId).stream()
                .filter(b -> "CONFIRMED".equals(b.status))
                .count();

        if (occupied >= slot.capacity) {
            throw new IllegalArgumentException(
                    "This time slot is fully booked.");
        }

        Booking booking = new Booking();
        booking.userId = userId;
        booking.slotId = slotId;
        booking.purpose = purpose;
        booking.status = "PENDING";

        Booking saved = bookingRepository.save(booking);

        recordStatus(saved.bookingId, null, "PENDING", userId,
                "Booking request submitted.");

        return saved;
    }

    @Transactional
    public Booking updateBookingStatus(
            Long bookingId, Long changedBy, String newStatus, String remarks) {

        if (!List.of("CONFIRMED", "REJECTED").contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Allowed status changes: CONFIRMED or REJECTED.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found."));

        if (!"PENDING".equals(booking.status)) {
            throw new IllegalArgumentException(
                    "Only pending bookings can be confirmed or rejected.");
        }

        if ("CONFIRMED".equals(newStatus)) {
            ExperimentSlot slot = slotRepository.findById(booking.slotId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Time slot not found."));

            long occupied = bookingRepository.findBySlotId(booking.slotId).stream()
                    .filter(b -> "CONFIRMED".equals(b.status))
                    .count();

            if (occupied >= slot.capacity) {
                throw new IllegalArgumentException(
                        "This time slot has reached its capacity.");
            }
        }

        String oldStatus = booking.status;
        booking.status = newStatus;
        Booking saved = bookingRepository.save(booking);

        recordStatus(bookingId, oldStatus, newStatus, changedBy, remarks);
        return saved;
    }

    @Transactional
    public Booking cancelBooking(
            Long bookingId, Long cancelledBy, String reason) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found."));

        if (!List.of("PENDING", "CONFIRMED").contains(booking.status)) {
            throw new IllegalArgumentException(
                    "This booking cannot be cancelled.");
        }

        String oldStatus = booking.status;
        booking.status = "CANCELLED";
        Booking saved = bookingRepository.save(booking);

        BookingCancellation cancellation = new BookingCancellation();
        cancellation.bookingId = bookingId;
        cancellation.cancelledBy = cancelledBy;
        cancellation.reason = reason;
        cancellationRepository.save(cancellation);

        recordStatus(bookingId, oldStatus, "CANCELLED",
                cancelledBy, reason);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found."));
    }

    @Transactional(readOnly = true)
    public List<BookingStatusHistory> getBookingHistory(Long bookingId) {
        return historyRepository.findByBookingIdOrderByChangedAtDesc(bookingId);
    }

    private void recordStatus(
            Long bookingId, String oldStatus, String newStatus,
            Long changedBy, String remarks) {

        BookingStatusHistory history = new BookingStatusHistory();
        history.bookingId = bookingId;
        history.oldStatus = oldStatus;
        history.newStatus = newStatus;
        history.changedBy = changedBy;
        history.remarks = remarks;
        historyRepository.save(history);
    }
}
