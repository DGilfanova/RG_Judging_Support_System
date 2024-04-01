package ru.itis.rgjudge.dto;

import lombok.Builder;
import ru.itis.rgjudge.dto.internal.ReportData;

import java.util.List;

@Builder
public record ElementReport(
        String elementName,
        Double elementScore,
        Boolean isValid,
        Double penalty,
        Double finalScore,
        String videoLink,
        List<ReportData> detailedEstimatorReport
) {
}
