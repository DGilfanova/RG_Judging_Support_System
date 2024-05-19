package ru.itis.rgjudge.service.estimator.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse.PoseData;
import ru.itis.rgjudge.dto.PoseResponse.Coordinate;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.enums.Criteria;
import ru.itis.rgjudge.dto.enums.DetectionQualityState;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.FrameInfo;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.PenaltyScorer;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.ArrayList;
import java.util.List;

import static ru.itis.rgjudge.db.enums.BodyPositionType.OPEN;
import static ru.itis.rgjudge.db.enums.TypeByExecution.STATIC_STANDING;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_FOOT_INDEX;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_HEEL;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_FOOT_INDEX;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_HEEL;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.DEGREES_180;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculate3DAngle;
import static ru.itis.rgjudge.utils.CoordinateUtils.checkNotDataEmission;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;
import static ru.itis.rgjudge.utils.CoordinateUtils.isCoordinateInFrame;
import static ru.itis.rgjudge.utils.DetectionQualityUtils.getDetectionQualityInPercentage;

@Component
@Order(5)
public class ReleveEstimator implements Estimator {

    private static final Double RELEVE_DEGREE = 45.0;

    private final PenaltyScorer penaltyScorer;
    private final RulesProperties rulesProperties;

    public ReleveEstimator(PenaltyScorer penaltyScorer, RulesProperties rulesProperties) {
        this.penaltyScorer = penaltyScorer;
        this.rulesProperties = rulesProperties;
    }

    @Override
    public boolean isApplicableToElement(Element element) {
        return STATIC_STANDING.equals(element.typeByExecution());
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseData> poseData,
                                             List<BodyPart> bodyParts,
                                             Element element,
                                             BodyPositionType bodyPositionType,
                                             FrameInfo frameInfo,
                                             Side handed) {
        var start = 0;
        var end = 0;
        var curAngle = 0.0;
        var maxAngle = 0.0;
        var isReleve = true;
        var isReleveRegistered = false;
        var detectionQualityList = new ArrayList<DetectionQualityState>();
        for (int i = 0; i < poseData.size(); i++) {
            var coordinates = poseData.get(i).getCoordinates();

            curAngle = switch (element.typeBySupportLeg()) {
                case BACK -> {
                    var rightHeelCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_HEEL);
                    var rightFootIndexCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_FOOT_INDEX);

                    var newAngle = calculateAngle(rightHeelCoordinate, rightFootIndexCoordinate, curAngle, bodyPositionType);
                    detectionQualityList.add(getDetectionQualityState(rightHeelCoordinate, rightFootIndexCoordinate, frameInfo, curAngle, newAngle));
                    yield  newAngle;
                }
                case FRONT, SIDE -> {
                    var leftHeelCoordinate = getCoordinate(coordinates, bodyParts, LEFT_HEEL);
                    var leftFootIndexCoordinate = getCoordinate(coordinates, bodyParts, LEFT_FOOT_INDEX);

                    var newAngle = calculateAngle(leftHeelCoordinate, leftFootIndexCoordinate, curAngle, bodyPositionType);
                    detectionQualityList.add(getDetectionQualityState(leftHeelCoordinate, leftFootIndexCoordinate, frameInfo, curAngle, newAngle));
                    yield newAngle;
                }
            };

            // Если релеве - isReleveRegistered = true и начинаем отсчет по времени,
            // в случае опускания на полную стопу (isReleve = false) повторно не считаем
            // Если не релеве и если релеве было зафиксировано (начали отсчет - start>0) - isReleve = false
            if (curAngle >= RELEVE_DEGREE) {
                if (start == 0) isReleveRegistered = true;
                if (isReleve) {
                    end = i;
                    if (start == 0) start = i;
                }
            } else {
                if (start > 0) isReleve = false;
            }
            // Сохраняем максимальной угол поднятия пятки
            if (curAngle > maxAngle) maxAngle = curAngle;
        }

        var duration = poseData.get(end).getTime() - poseData.get(start).getTime();
        var isValid = true;
        if (isReleveRegistered) {
            isValid = duration - rulesProperties.balanceFixationDuration() > 0;
        }

        var penalty = NO_PENALTY;
        if (!isReleveRegistered) penalty = penaltyScorer.getPenaltyForNotReleve();
        if (!isValid) penalty = penaltyScorer.getPenaltyForFallingFromReleve();

        return EstimatorResponse.builder()
            .criteria(Criteria.RELEVE)
            .isValid(Boolean.TRUE)
            .penalty(penalty)
            .reportData(prepareReportData(isValid, isReleveRegistered, penalty, maxAngle, duration, detectionQualityList))
            .estimationType(EstimationType.FULL_CHECK)
            .build();
    }

    // (c1, c2) - вектор стопы от пятки к большому пальцу
    private Double calculateAngle(Coordinate c1, Coordinate c2, Double previousAngle, BodyPositionType bodyPositionType) {
        // Проекция точки с1 на "пол"
        var c3 = Coordinate.builder()
            .x(c1.getX())
            .y(c2.getY())
            .z(c1.getZ())
            .build();
        double angle = calculate3DAngle(c1, c2, c3);

        // Если положение открытое - стопа всегда смотрит влево, закрытое - вправо, по этому определяем, когда релеве переходит за 90 град
        return OPEN.equals(bodyPositionType)
            ? c1.getX() <= c2.getX() && checkNotDataEmission(DEGREES_180 - angle, previousAngle) ? DEGREES_180 - angle : angle
            : c1.getX() >= c2.getX() && checkNotDataEmission(DEGREES_180 - angle, previousAngle) ? DEGREES_180 - angle : angle;
    }

    private ReportData prepareReportData(Boolean isValid, Boolean isReleveRegistered, Double penalty, Double maxAngle, Double duration, ArrayList<DetectionQualityState> detectionQualityList) {
        var report = ReportData.builder()
            .isCounted(Boolean.TRUE.toString())
            .expectedBehavior("Длительность фиксации в релеве = %s сек"
                .formatted(DECIMAL_FORMAT.format(rulesProperties.balanceFixationDuration())))
            .estimatorName("Оценка релеве");

        if (isReleveRegistered) {
            if (isValid) {
                report.actualBehavior("Релеве зафиксировано и засчитано. Длительность фиксации в релеве = %s сек".formatted(DECIMAL_FORMAT.format(duration)));
            } else {
                report.actualBehavior("Релеве зафиксировано, но не засчитано. Длительность фиксации в релеве = %s сек. Максимальный угол поднятия пятки = %s°"
                    .formatted(DECIMAL_FORMAT.format(duration), Math.round(maxAngle)));
            }
        } else {
            report.actualBehavior("Релеве не зафиксировано. Максимальный угол поднятия пятки = %s°".formatted(Math.round(maxAngle)));
        }
        return report.penalty(DECIMAL_FORMAT.format(penalty))
            .detectionQuality(Math.round(getDetectionQualityInPercentage(detectionQualityList)))
            .build();
    }

    private DetectionQualityState getDetectionQualityState(Coordinate heelCoordinate, Coordinate footIndexCoordinate,
                                                           FrameInfo frameInfo, Double previousAngle, Double newAngle) {
        if (!isCoordinateInFrame(heelCoordinate, frameInfo) || !isCoordinateInFrame(footIndexCoordinate, frameInfo)) {
            return DetectionQualityState.NOT_DETECTED;
        }
        var angleDif = previousAngle > 0 ? Math.abs(previousAngle - newAngle) : 0;

        // Вычисляем максимальную скорость движения корпуса
        var degreeVelocity = RELEVE_DEGREE / frameInfo.fps();
        if (angleDif >= degreeVelocity) {
            return angleDif > 2 * degreeVelocity
                ? DetectionQualityState.EMISSION
                : DetectionQualityState.POSSIBLE_EMISSION;
        }
        return DetectionQualityState.GOOD;
    }
}
