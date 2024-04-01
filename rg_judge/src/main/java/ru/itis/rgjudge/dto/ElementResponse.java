package ru.itis.rgjudge.dto;

import java.time.LocalDateTime;

public record ElementResponse(
        Long id,
        String officialNumber,
        String name,
        Double value,
        LocalDateTime createdAt
) {
}
