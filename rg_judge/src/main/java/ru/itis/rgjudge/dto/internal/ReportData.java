package ru.itis.rgjudge.dto.internal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportData {
    String estimatorName;
    String expectedBehavior;
    String actualBehavior;
    String penalty;
    String isCounted;
    Long detectionQuality;
    Boolean isIgnore;
}
