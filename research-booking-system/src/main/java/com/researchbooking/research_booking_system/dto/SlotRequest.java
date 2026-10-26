package com.researchbooking.research_booking_system.dto;

import java.time.LocalDateTime;

public record SlotRequest(
    LocalDateTime startTime,
    LocalDateTime endTime,
    Integer capacity
) {
}
