package ru.itis.rgjudge.dto.internal;

import lombok.Builder;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.dto.PoseResponse;

import java.util.List;

@Builder
public record EstimatorResponse(
        ReportData reportData,
        Boolean isValid,
        Double penalty,
        EstimationType estimationType,
        List<PoseResponse.PoseData> elementExecutionPoseData
) {
}
