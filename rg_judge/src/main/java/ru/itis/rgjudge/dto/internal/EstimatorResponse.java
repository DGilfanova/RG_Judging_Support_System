package ru.itis.rgjudge.dto.internal;

import lombok.Builder;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.Criteria;

import java.util.List;

@Builder
public record EstimatorResponse(
    Criteria criteria,
    ReportData reportData,
    Boolean isValid,
    Double penalty,
    EstimationType estimationType,
    List<PoseResponse.PoseData> elementExecutionPoseData
) {
}
