
package com.researchbooking.research_booking_system.repository;

import com.researchbooking.research_booking_system.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findBySlotId(Long slotId);
    List<Booking> findByStatus(String status);
    List<Booking> findByUserIdAndStatus(Long userId, String status);
}
