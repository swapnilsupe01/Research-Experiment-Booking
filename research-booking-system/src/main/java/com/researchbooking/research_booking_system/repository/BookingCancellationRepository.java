
package com.researchbooking.research_booking_system.repository;

import com.researchbooking.research_booking_system.entity.BookingCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookingCancellationRepository
        extends JpaRepository<BookingCancellation, Long> {
    Optional<BookingCancellation> findByBookingId(Long bookingId);
}
