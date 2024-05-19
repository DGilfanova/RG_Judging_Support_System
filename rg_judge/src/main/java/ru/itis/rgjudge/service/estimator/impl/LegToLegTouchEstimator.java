package ru.itis.rgjudge.service.estimator.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
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
import ru.itis.rgjudge.dto.enums.Criteria;
import ru.itis.rgjudge.dto.enums.DetectionQualityState;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.FrameInfo;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.ArrayList;
import java.util.List;

import static ru.itis.rgjudge.dto.enums.BodyPart.getFootIndex;
import static ru.itis.rgjudge.dto.enums.BodyPart.getHip;
import static ru.itis.rgjudge.dto.enums.BodyPart.getKnee;
import static ru.itis.rgjudge.utils.Constant.CONTROVERSIAL_SITUATION_PROBABILITY_ACCURACY;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculate2DDistance;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateDistanceFromPointToLine;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;
import static ru.itis.rgjudge.utils.CoordinateUtils.isCoordinateInFrame;
import static ru.itis.rgjudge.utils.DetectionQualityUtils.getDetectionQualityInPercentage;

@Component
@Order(8)
public class LegToLegTouchEstimator implements Estimator {

    private static final Double THIGH_WIDTH_TO_SHIN_LENGTH_PROPORTION = 0.37;

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.legToLegTouchCriteria().isActive();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseData> poseData,
                                             List<BodyPart> bodyParts,
                                             Element element,
                                             BodyPositionType bodyPositionType,
                                             FrameInfo frameInfo,
                                             Side handed) {
        var grabbedLegSide = element.typeBySupportLeg().equals(TypeBySupportLeg.BACK) ? Side.RIGHT : Side.LEFT;

        var isCorrect = false;
        var minDistance = 0.0;
        var thighWidth = 0.0;

        var detectionQualityList = new ArrayList<DetectionQualityState>();
        var qualityParams = new QualityParams(0d, 0d);
        var isControversialSituation = false;

        for (PoseData poseDatum : poseData) {
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

            // Вычисляем, что нога находится в спорной зоне (в допустимой и недопустимой)
            if (Math.abs(distance - thighWidth / 2) < thighWidth) {
                isControversialSituation = true;
            }

            if (distance <= thighWidth / 2) {
                isCorrect = true;
            }

            setDetectionQualityState(detectionQualityList, hipCoordinate, kneeCoordinate, footCoordinate, qualityParams, frameInfo);
        }

        return EstimatorResponse.builder()
            .criteria(Criteria.LEG_TO_LEG_TOUCH)
            .isValid(isCorrect)
            .penalty(NO_PENALTY)
            .reportData(prepareReportData(isCorrect, minDistance, detectionQualityList, isControversialSituation))
            .estimationType(EstimationType.FULL_CHECK)
            .build();
    }

    private ReportData prepareReportData(Boolean isCorrect, Double minDistance, ArrayList<DetectionQualityState> detectionQualityList, boolean isControversialSituation) {
        var detectionQuality = getDetectionQualityInPercentage(detectionQualityList);
        if (isControversialSituation) {
            detectionQuality *= CONTROVERSIAL_SITUATION_PROBABILITY_ACCURACY;
        }

        return ReportData.builder()
            .estimatorName("Касание ноги ногой")
            .expectedBehavior("Стопа должна касаться бедра")
            .actualBehavior(isCorrect
                ? "Стопа касается бедра"
                : "Стопа не касается бедра. Минимальное достигнутое расстояние = %s px".formatted(Math.round(minDistance)))
            .penalty(DECIMAL_FORMAT.format(NO_PENALTY))
            .isCounted(isCorrect.toString())
            .detectionQuality(Math.round(detectionQuality))
            .build();
    }

    private void setDetectionQualityState(List<DetectionQualityState> detectionQualityList, Coordinate hipCoordinate,
                                          Coordinate kneeCoordinate, Coordinate footCoordinate,
                                          QualityParams qualityParams, FrameInfo frameInfo) {
        if (qualityParams.getMaxVelocity().equals(0d)) {
            qualityParams.setMaxVelocity(getMaxVelocity(hipCoordinate, kneeCoordinate, frameInfo));
        }

        var distance = calculate2DDistance(kneeCoordinate, footCoordinate);

        if (qualityParams.getCurDistance().equals(0d)) {
            qualityParams.setCurDistance(distance);
            detectionQualityList.add(DetectionQualityState.GOOD);
            return;
        }

        if (!isCoordinateInFrame(hipCoordinate, frameInfo) || !isCoordinateInFrame(kneeCoordinate, frameInfo) || !isCoordinateInFrame(footCoordinate, frameInfo)) {
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

    // Вычисляем максимальную скорость ноги относительно другой ноги (расстояние за кадр)
    private Double getMaxVelocity(Coordinate hipCoordinate, Coordinate kneeCoordinate, FrameInfo frameInfo) {
        var thighLength = calculate2DDistance(hipCoordinate, kneeCoordinate);

        // за треть секунды стопа может отдалиться от колена на длину = thighLength (подробнее описать)
        return thighLength / (frameInfo.fps() * 0.3);
    }

    @Data
    @AllArgsConstructor
    private class QualityParams {
        private Double curDistance;
        private Double maxVelocity;
    }
}
