package com.researchbooking.research_booking_system.dto;

public record ExperimentRequest(
    String title,
    String description,
    String laboratoryName,
    Integer durationMinutes
) {
}
