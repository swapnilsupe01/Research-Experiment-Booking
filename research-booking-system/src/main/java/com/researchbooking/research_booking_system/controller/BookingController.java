
package com.researchbooking.research_booking_system.controller;

import com.researchbooking.research_booking_system.entity.Booking;
import com.researchbooking.research_booking_system.entity.BookingStatusHistory;
import com.researchbooking.research_booking_system.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Request a booking
    @PostMapping
    public ResponseEntity<Booking> requestBooking(
            @RequestBody Map<String, Object> request) {

        Long userId = Long.valueOf(request.get("userId").toString());
        Long slotId = Long.valueOf(request.get("slotId").toString());
        String purpose = request.get("purpose").toString();

        Booking booking =
                bookingService.requestBooking(userId, slotId, purpose);

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    // Get all bookings for a particular user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsForUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                bookingService.getBookingsForUser(userId));
    }

    // Get a particular booking
    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.getBookingById(bookingId));
    }

    // Confirm or reject a pending booking
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<Booking> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, Object> request) {

        Long changedBy =
                Long.valueOf(request.get("changedBy").toString());

        String status = request.get("status").toString();
        String remarks = request.get("remarks") == null
                ? null : request.get("remarks").toString();

        Booking booking = bookingService.updateBookingStatus(
                bookingId, changedBy, status, remarks);

        return ResponseEntity.ok(booking);
    }

    // Cancel a booking
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long bookingId,
            @RequestBody Map<String, Object> request) {

        Long cancelledBy =
                Long.valueOf(request.get("cancelledBy").toString());

        String reason = request.get("reason").toString();

        Booking booking = bookingService.cancelBooking(
                bookingId, cancelledBy, reason);

        return ResponseEntity.ok(booking);
    }

    // View booking status history
    @GetMapping("/{bookingId}/history")
    public ResponseEntity<List<BookingStatusHistory>> getBookingHistory(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.getBookingHistory(bookingId));
    }
}
