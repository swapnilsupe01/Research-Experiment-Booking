
package com.researchbooking.research_booking_system.repository;

import com.researchbooking.research_booking_system.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingStatusHistoryRepository
        extends JpaRepository<BookingStatusHistory, Long> {
    List<BookingStatusHistory> findByBookingIdOrderByChangedAtDesc(Long bookingId);
}
