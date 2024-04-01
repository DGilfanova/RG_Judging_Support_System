package ru.itis.rgjudge.db.model;

import lombok.Builder;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;

import java.time.LocalDateTime;

@Builder
public record ElementShortInfo(
        Integer id,
        String officialNumber,
        String name,
        Double value,
        TypeBySupportLeg typeBySupportLeg,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
