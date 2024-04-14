package ru.itis.rgjudge.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.itis.rgjudge.dto.internal.ReportData;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ElementReport {
        String elementName;
        Double elementScore;
        Boolean isValid;
        Double penalty;
        Double finalScore;
        String videoLink;
        List<ReportData> detailedEstimatorReport;
}
