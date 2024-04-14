package ru.itis.rgjudge.service.estimator.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse.PoseData;
import ru.itis.rgjudge.dto.PoseResponse.Coordinate;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.List;

import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_SHOULDER;
import static ru.itis.rgjudge.dto.enums.BodyPart.NOSE;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_SHOULDER;
import static ru.itis.rgjudge.dto.enums.BodyPart.getFootIndex;
import static ru.itis.rgjudge.dto.enums.BodyPart.getHip;
import static ru.itis.rgjudge.dto.enums.BodyPart.getKnee;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAverage;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculate2DDistance;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateDistanceFromPointToLine;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;

@Component
@Order(7)
public class HeadToLegTouchEstimator implements Estimator {

    private static final Double THIGH_WIDTH_TO_SHIN_LENGTH_PROPORTION = 0.37;
    private static final Double HEAD_TO_THIGH_LENGTH_PROPORTION = 0.5;
    private static final Double BEAM_TO_HEAD_LENGTH_PROPORTION = 0.125;

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.headToLegTouchCriteria().isActive();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseData> poseData, List<BodyPart> bodyParts, Element element, BodyPositionType bodyPositionType) {
        var type = element.headToLegTouchCriteria().type();
        var grabbedLegSide = element.typeBySupportLeg().equals(TypeBySupportLeg.BACK) ? Side.LEFT : Side.RIGHT;

        var isCorrect = true;
        var minDistance = 0.0;
        var headHeight = 0.0;
        var thighWidth = 0.0;
        for (PoseData poseDatum : poseData) {
            var coordinates = poseDatum.getCoordinates();
            var hipCoordinate = getCoordinate(coordinates, bodyParts, getHip(grabbedLegSide));
            var kneeCoordinate = getCoordinate(coordinates, bodyParts, getKnee(grabbedLegSide));

            if (thighWidth == 0.0)
                thighWidth = THIGH_WIDTH_TO_SHIN_LENGTH_PROPORTION * calculate2DDistance(hipCoordinate, kneeCoordinate);
            if (headHeight == 0.0)
                headHeight = HEAD_TO_THIGH_LENGTH_PROPORTION * thighWidth;

            var noseCoordinate = getCoordinate(coordinates, bodyParts, NOSE);
            var shoulderCoordinate = calculateAverage(
                getCoordinate(coordinates, bodyParts, LEFT_SHOULDER),
                getCoordinate(coordinates, bodyParts, RIGHT_SHOULDER)
            );

            // В зависимости от типа касания головы ногой разная обработка
            var distance = 0.0;
            var legIsTouched = switch (type) {
                case SHALLOW_POSTURE -> {
                    var footCoordinate = getCoordinate(coordinates, bodyParts, getFootIndex(grabbedLegSide));
                    distance = calculateDistanceBetweenHeadAndFoot(footCoordinate, shoulderCoordinate, noseCoordinate, headHeight);
                    yield distance <= headHeight * BEAM_TO_HEAD_LENGTH_PROPORTION;
                }
                case DEEP_POSTURE -> {
                    distance = calculateDistanceBetweenHeadAndHip(hipCoordinate, kneeCoordinate, shoulderCoordinate, noseCoordinate, headHeight, thighWidth);
                    yield distance <= headHeight * BEAM_TO_HEAD_LENGTH_PROPORTION;
                }
            };

            if (minDistance == 0.0 || distance < minDistance) {
                minDistance = distance;
            }

            isCorrect = isCorrect && legIsTouched;
        }

        return EstimatorResponse.builder()
            .isValid(isCorrect)
            .penalty(NO_PENALTY)
            .reportData(prepareReportData(element, isCorrect, minDistance))
            .estimationType(EstimationType.FULL_CHECK)
            .build();
    }

    private Double calculateDistanceBetweenHeadAndFoot(Coordinate footCoordinate,
                                                       Coordinate shoulderCoordinate,
                                                       Coordinate noseCoordinate,
                                                       Double headHeight) {
        var shoulderNoseLength = calculate2DDistance(shoulderCoordinate, noseCoordinate);
        if (shoulderNoseLength < headHeight) {
            // Точка носа определена верно, вычисления точнее
            var noseFootLength = calculate2DDistance(noseCoordinate, footCoordinate);
            return Math.abs(noseFootLength - shoulderNoseLength);
        } else {
            // Точка носа определена неверно, при вычислениях игнорируем ее
            return Math.abs(calculate2DDistance(shoulderCoordinate, footCoordinate) - headHeight);
        }
    }

    private Double calculateDistanceBetweenHeadAndHip(Coordinate hipCoordinate,
                                                      Coordinate kneeCoordinate,
                                                      Coordinate shoulderCoordinate,
                                                      Coordinate noseCoordinate,
                                                      Double headHeight,
                                                      Double thighWidth) {
        double shoulderNoseLength = calculate2DDistance(shoulderCoordinate, noseCoordinate);
        if (shoulderNoseLength < headHeight) {
            // Точка носа определена верно, вычисления точнее
            return calculateDistanceFromPointToLine(hipCoordinate, kneeCoordinate, noseCoordinate) - shoulderNoseLength - thighWidth / 2;
        } else {
            // Точка носа определена неверно, при вычислениях игнорируем ее
            return calculateDistanceFromPointToLine(hipCoordinate, kneeCoordinate, shoulderCoordinate) - headHeight - thighWidth / 2;
        }
    }

    private ReportData prepareReportData(Element element, Boolean isCorrect, Double minDistance) {
        var report = ReportData.builder()
            .estimatorName("Касание головы ногой")
            .isCounted(isCorrect.toString());
        switch (element.headToLegTouchCriteria().type()) {
            case SHALLOW_POSTURE ->
                report.expectedBehavior("Голова должна касаться стопы")
                    .actualBehavior(isCorrect
                        ? "Голова коснулась стопы"
                        : "Голова не коснулась стопы. Минимальное достигнутое расстояние = %s px".formatted(Math.round(minDistance))
                    );
            case DEEP_POSTURE ->
                report.expectedBehavior("Голова должна касаться бедра")
                    .actualBehavior(isCorrect
                        ? "Голова коснулась бедра"
                        : "Голова не коснулась бедра. Минимальное достигнутое расстояние = %s px".formatted(Math.round(minDistance))
                    );
        }
        return report.penalty(DECIMAL_FORMAT.format(NO_PENALTY))
            .build();
    }
}
