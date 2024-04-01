package ru.itis.rgjudge.dto.internal;

import lombok.Builder;

@Builder
public record ReportData(
        String estimatorName,
        String expectedBehavior,
        String actualBehavior,
        String penalty,
        Boolean isCounted
) {
}
