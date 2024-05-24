package ru.itis.rgjudge.service.estimator.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse.PoseData;
import ru.itis.rgjudge.dto.PoseResponse.Coordinate;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.enums.Criteria;
import ru.itis.rgjudge.dto.enums.DetectionQualityState;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.FrameInfo;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.ArrayList;
import java.util.List;

import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_SHOULDER;
import static ru.itis.rgjudge.dto.enums.BodyPart.NOSE;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_SHOULDER;
import static ru.itis.rgjudge.dto.enums.BodyPart.getFootIndex;
import static ru.itis.rgjudge.dto.enums.BodyPart.getHip;
import static ru.itis.rgjudge.dto.enums.BodyPart.getKnee;
import static ru.itis.rgjudge.utils.Constant.CONTROVERSIAL_SITUATION_PROBABILITY_ACCURACY;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.MIN_GOOD_DETECTION_PROBABILITY;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculate3DDistance;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAverage;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculate2DDistance;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateDistanceFromPointToLine;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;
import static ru.itis.rgjudge.utils.CoordinateUtils.isCoordinateInFrame;
import static ru.itis.rgjudge.utils.DetectionQualityUtils.getDetectionQualityInPercentage;

@Component
@Order(7)
public class HeadToLegTouchEstimator implements Estimator {

    private static final Double THIGH_WIDTH_TO_THIGH_LENGTH_PROPORTION = 0.37;
    private static final Double HEAD_TO_THIGH_LENGTH_PROPORTION = 0.8;
    private static final Double BEAM_TO_HEAD_LENGTH_PROPORTION = 0.125;

    private final RulesProperties rulesProperties;

    public HeadToLegTouchEstimator(RulesProperties rulesProperties) {
        this.rulesProperties = rulesProperties;
    }

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.headToLegTouchCriteria().isActive();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseData> poseData,
                                             List<BodyPart> bodyParts,
                                             Element element,
                                             BodyPositionType bodyPositionType,
                                             FrameInfo frameInfo,
                                             Side handed) {
        var type = element.headToLegTouchCriteria().type();
        var grabbedLegSide = element.typeBySupportLeg().equals(TypeBySupportLeg.BACK) ? Side.LEFT : Side.RIGHT;

        var start = 0;
        var end = 0;
        var minDistance = 0.0;
        var thighLength = 0.0;
        var headHeight = 0.0;
        var thighWidth = 0.0;

        var detectionQualityList = new ArrayList<DetectionQualityState>();
        var qualityParams = new QualityParams(0d, 0d);
        var isControversialSituation = false;

        for (int i = 0; i < poseData.size(); i++) {
            var coordinates = poseData.get(i).getCoordinates();
            var hipCoordinate = getCoordinate(coordinates, bodyParts, getHip(grabbedLegSide));
            var kneeCoordinate = getCoordinate(coordinates, bodyParts, getKnee(grabbedLegSide));
            var footCoordinate = getCoordinate(coordinates, bodyParts, getFootIndex(grabbedLegSide));

            if (thighLength == 0.0)
                thighLength = calculate2DDistance(hipCoordinate, kneeCoordinate);
            if (thighWidth == 0.0)
                thighWidth = THIGH_WIDTH_TO_THIGH_LENGTH_PROPORTION * thighLength;
            if (headHeight == 0.0)
                headHeight = HEAD_TO_THIGH_LENGTH_PROPORTION * thighLength;

            var noseCoordinate = getCoordinate(coordinates, bodyParts, NOSE);
            var shoulderCoordinate = calculateAverage(
                getCoordinate(coordinates, bodyParts, LEFT_SHOULDER),
                getCoordinate(coordinates, bodyParts, RIGHT_SHOULDER)
            );

            // В зависимости от типа касания головы ногой разная обработка
            double distance = switch (type) {
                case SHALLOW_POSTURE ->
                    calculateDistanceBetweenHeadAndFoot(footCoordinate, shoulderCoordinate, noseCoordinate, headHeight);
                case DEEP_POSTURE ->
                    calculateDistanceBetweenHeadAndHip(hipCoordinate, kneeCoordinate, shoulderCoordinate, noseCoordinate, headHeight, thighWidth);
            };

            // Вычисляем, что нога находится в спорной зоне (в допустимой и недопустимой)
            if (Math.abs(distance - headHeight) < headHeight * (1 - BEAM_TO_HEAD_LENGTH_PROPORTION) * 2) {
                isControversialSituation = true;
            }

            var legIsTouched = distance <= headHeight * BEAM_TO_HEAD_LENGTH_PROPORTION;
            if (legIsTouched) {
                if (start == 0) start = i;
            } else {
                if (minDistance == 0.0 || distance < minDistance) {
                    minDistance = distance;
                }
                end = i;
            }

            setDetectionQualityState(detectionQualityList, coordinates, shoulderCoordinate, footCoordinate, qualityParams, bodyParts, frameInfo);
        }

        var duration = start > 0 ? poseData.get(end > 0 ? end : poseData.size() - 1).getTime() - poseData.get(start).getTime() : 0.0;
        var isCorrect = duration - rulesProperties.balanceFixationDuration() > 0;

        return EstimatorResponse.builder()
            .criteria(Criteria.HEAD_TO_LEG_TOUCH)
            .isValid(isCorrect)
            .penalty(NO_PENALTY)
            .reportData(prepareReportData(element, isCorrect, minDistance, duration, detectionQualityList, isControversialSituation))
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

    private ReportData prepareReportData(Element element, Boolean isCorrect, Double minDistance, Double duration, ArrayList<DetectionQualityState> detectionQualityList, boolean isControversialSituation) {
        var report = ReportData.builder()
            .estimatorName("Касание головы ногой")
            .isCounted(isCorrect.toString());
        switch (element.headToLegTouchCriteria().type()) {
            case SHALLOW_POSTURE -> report.expectedBehavior("Голова должна касаться стопы")
                .actualBehavior(isCorrect
                    ? "Голова коснулась стопы"
                    : "Голова не коснулась стопы. Минимальное достигнутое расстояние = %s px. Длительность касания = %s сек."
                        .formatted(Math.round(minDistance), DECIMAL_FORMAT.format(duration))
                );
            case DEEP_POSTURE -> report.expectedBehavior("Голова должна касаться бедра")
                .actualBehavior(isCorrect
                    ? "Голова коснулась бедра"
                    : "Голова не коснулась бедра. Минимальное достигнутое расстояние = %s px. Длительность касания = %s сек."
                        .formatted(Math.round(minDistance), DECIMAL_FORMAT.format(duration))
                );
        }

        double detectionQuality = getDetectionQualityInPercentage(detectionQualityList);
        if (isControversialSituation && detectionQuality < MIN_GOOD_DETECTION_PROBABILITY) {
            detectionQuality *= CONTROVERSIAL_SITUATION_PROBABILITY_ACCURACY;
        }

        return report.penalty(DECIMAL_FORMAT.format(NO_PENALTY))
            .detectionQuality(Math.round(detectionQuality))
            .build();
    }

    private void setDetectionQualityState(List<DetectionQualityState> detectionQualityList, List<Coordinate> coordinates,
                                          Coordinate shoulderCoordinate, Coordinate footCoordinate,
                                          QualityParams qualityParams, List<BodyPart> bodyParts, FrameInfo frameInfo) {
        if (qualityParams.getMaxVelocity().equals(0d)) {
            qualityParams.setMaxVelocity(getMaxVelocity(coordinates, bodyParts, frameInfo));
        }

        var distance = calculate2DDistance(shoulderCoordinate, footCoordinate);

        if (qualityParams.getCurDistance().equals(0d)) {
            qualityParams.setCurDistance(distance);
            detectionQualityList.add(DetectionQualityState.GOOD);
            return;
        }

        if (!isCoordinateInFrame(shoulderCoordinate, frameInfo) || !isCoordinateInFrame(footCoordinate, frameInfo)) {
            detectionQualityList.add(DetectionQualityState.NOT_DETECTED);
            return;
        }

        var dif = Math.abs(distance - qualityParams.getCurDistance());
        qualityParams.setCurDistance(distance);

        if (dif > qualityParams.getMaxVelocity()) {
            var state = dif > 2 * qualityParams.getMaxVelocity()
                ? DetectionQualityState.EMISSION
                : DetectionQualityState.POSSIBLE_EMISSION;
            detectionQualityList.add(state);
            return;
        }

        detectionQualityList.add(DetectionQualityState.GOOD);
    }

    // Вычисляем максимальную скорость руки относительно ноги (расстояние за кадр)
    private Double getMaxVelocity(List<Coordinate> coordinates, List<BodyPart> bodyParts, FrameInfo frameInfo) {
        var hipCoordinate = calculateAverage(
            getCoordinate(coordinates, bodyParts, LEFT_HIP),
            getCoordinate(coordinates, bodyParts, RIGHT_HIP)
        );
        var shoulderCoordinate = calculateAverage(
            getCoordinate(coordinates, bodyParts, LEFT_SHOULDER),
            getCoordinate(coordinates, bodyParts, RIGHT_SHOULDER)
        );
        // по базовым пропорциям человека длина туловища примерно равна двум головам
        var headLength = calculate3DDistance(hipCoordinate, shoulderCoordinate) / 2;

        // максимальное расстояние от шеи до стопы
        var maxLength = headLength * 7;

        // за 1.1 секунды максимальное расстояние может быть достигнуто (подробнее описать)
        return maxLength / (frameInfo.fps() * 1.1);
    }

    @Data
    @AllArgsConstructor
    private class QualityParams {
        private Double curDistance;
        private Double maxVelocity;
    }
}
