package ru.itis.rgjudge.service.estimator.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.List;

import static ru.itis.rgjudge.dto.enums.BodyPart.getFootIndex;
import static ru.itis.rgjudge.dto.enums.BodyPart.getHip;
import static ru.itis.rgjudge.dto.enums.BodyPart.getKnee;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculate2DDistance;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateDistanceFromPointToLine;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;

@Component
@Order(8)
public class LegToLegTouchEstimator implements Estimator {

    private static final Double THIGH_WIDTH_TO_SHIN_LENGTH_PROPORTION = 0.37;

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.legToLegTouchCriteria().isActive();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseResponse.PoseData> poseData, List<BodyPart> bodyParts, Element element, BodyPositionType bodyPositionType) {
        var grabbedLegSide = element.typeBySupportLeg().equals(TypeBySupportLeg.BACK) ? Side.LEFT : Side.RIGHT;

        var isCorrect = true;
        var minDistance = 0.0;
        var thighWidth = 0.0;
        for (PoseResponse.PoseData poseDatum : poseData) {
            var coordinates = poseDatum.getCoordinates();
            var hipCoordinate = getCoordinate(coordinates, bodyParts, getHip(grabbedLegSide));
            var kneeCoordinate = getCoordinate(coordinates, bodyParts, getKnee(grabbedLegSide));
            var footCoordinate = getCoordinate(coordinates, bodyParts, getFootIndex(grabbedLegSide.getOpposite()));

            if (thighWidth == 0.0)
                thighWidth = THIGH_WIDTH_TO_SHIN_LENGTH_PROPORTION * calculate2DDistance(hipCoordinate, kneeCoordinate);

            var distance = calculateDistanceFromPointToLine(hipCoordinate, kneeCoordinate, footCoordinate);

            if (minDistance == 0.0 || distance < minDistance) {
                minDistance = distance;
            }

            isCorrect = isCorrect && (distance <= thighWidth / 2);
        }

        return EstimatorResponse.builder()
            .isValid(isCorrect)
            .penalty(NO_PENALTY)
            .reportData(prepareReportData(isCorrect, minDistance))
            .estimationType(EstimationType.FULL_CHECK)
            .build();
    }

    private ReportData prepareReportData(Boolean isCorrect, Double minDistance) {
        return ReportData.builder()
            .estimatorName("Касание ноги ногой")
            .expectedBehavior("Стопа должна касаться бедра")
            .actualBehavior(isCorrect
                ? "Стопа касается бедра"
                : "Стопа не касается бедра. Минимальное достигнутое расстояние = %s px".formatted(Math.round(minDistance)))
            .penalty(DECIMAL_FORMAT.format(NO_PENALTY))
            .isCounted(isCorrect.toString())
            .build();
    }
}
