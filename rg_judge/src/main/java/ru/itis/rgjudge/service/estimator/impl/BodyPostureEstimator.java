package ru.itis.rgjudge.service.estimator.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.enums.Criteria;
import ru.itis.rgjudge.dto.enums.DetectionQualityState;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.PoseResponse.Coordinate;
import ru.itis.rgjudge.dto.PoseResponse.PoseData;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.FrameInfo;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.PenaltyScorer;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.ArrayList;
import java.util.List;

import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_ANKLE;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_SHOULDER;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_ANKLE;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_SHOULDER;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.DEGREES_180;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAngle;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAverage;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;
import static ru.itis.rgjudge.utils.CoordinateUtils.isCoordinateInFrame;
import static ru.itis.rgjudge.utils.DetectionQualityUtils.getDetectionQualityInPercentage;

@Component
@Order(2)
public class BodyPostureEstimator implements Estimator {

    private static final Logger logger = LoggerFactory.getLogger(BodyPostureEstimator.class);

    private final PenaltyScorer penaltyScorer;
    private final RulesProperties rulesProperties;

    public BodyPostureEstimator(PenaltyScorer penaltyScorer, RulesProperties rulesProperties) {
        this.penaltyScorer = penaltyScorer;
        this.rulesProperties = rulesProperties;
    }

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.bodyPostureCriteria().isActive();
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
        var wasFixed = false;
        var closerAngleDif = 0.0;
        var curAngle = 0.0;
        var detectionQualityList = new ArrayList<DetectionQualityState>();
        for (int i = 0; i < poseData.size(); i++) {
            var coordinates = poseData.get(i).getCoordinates();
            var shoulderCoordinate = calculateAverage(
                getCoordinate(coordinates, bodyParts, LEFT_SHOULDER),
                getCoordinate(coordinates, bodyParts, RIGHT_SHOULDER)
            );
            Coordinate supportLegCoordinate;
            Coordinate hipCoordinate;
            if (TypeBySupportLeg.BACK.equals(element.typeBySupportLeg())) {
                // Если тип элемента = заднее, вычисляем по правым лодыжке и бедру
                supportLegCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_ANKLE);
                hipCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_HIP);
            } else {
                // Если тип элемента = переднее, вычисляем по левым колену и бедру
                supportLegCoordinate = getCoordinate(coordinates, bodyParts, LEFT_ANKLE);
                hipCoordinate = getCoordinate(coordinates, bodyParts, LEFT_HIP);
            }

            var newAngle = calculateAngle(supportLegCoordinate, hipCoordinate, shoulderCoordinate, curAngle, bodyPositionType);
            detectionQualityList.add(getDetectionQualityState(supportLegCoordinate, hipCoordinate, shoulderCoordinate, frameInfo, curAngle, newAngle, element.bodyPostureCriteria()));
            curAngle = newAngle;

            // Если достигнуто необходимое положение тела и это первый заход (wasFixed == false), начинаем отсчет
            // Иначе вычисляем угол максимального приближения к нужному положению.
            // А в случае если идет отчет, но положение тела нарушилось - wasFixed = true
            if (curAngle >= element.bodyPostureCriteria().minDegree() && curAngle <= element.bodyPostureCriteria().maxDegree()) {
                if (!wasFixed) {
                    end = i;
                    if (start == 0) start = i;
                }
            } else {
                var currentAngleDif = countCloserAngleDif(curAngle, element.bodyPostureCriteria().minDegree(), element.bodyPostureCriteria().maxDegree());
                if (closerAngleDif == 0.0 || currentAngleDif < closerAngleDif) closerAngleDif = currentAngleDif;
                if (start > 0) wasFixed = true;
            }
        }
        logger.info("Element execution start frame = {}, end frame = {}", start, end);
        var duration = poseData.get(end).getTime() - poseData.get(start).getTime();

        // Элемент засчитан, если длительность фиксации положения тела не менее необходимого
        var isValid = duration - rulesProperties.balanceFixationDuration() > 0;

        // Применяем сбавку за отсутствие достижения нужного положения тела на протяжении всего элемента
        var penalty = 0.0;
        if (!isValid && duration == 0.0) {
            penalty = penaltyScorer.getPenaltyByAngle(closerAngleDif);
        }

        return EstimatorResponse.builder()
            .criteria(Criteria.BODY_POSITION)
            .isValid(isValid)
            .penalty(penalty)
            .elementExecutionPoseData(poseData.subList(start, end))
            .reportData(prepareReportData(element, duration, penalty, closerAngleDif, isValid, detectionQualityList))
            .estimationType(EstimationType.FULL_CHECK)
            .build();
    }

    private Double countCloserAngleDif(Double curAngle, Float minAngle, Float maxAngle) {
        return Math.min(Math.abs(minAngle - curAngle), Math.abs(maxAngle - curAngle));
    }

    private ReportData prepareReportData(Element element, Double duration, Double penalty, Double angleDif, Boolean isValid, ArrayList<DetectionQualityState> detectionQualityList) {
        var report = ReportData.builder()
            .estimatorName("Оценка положения корпуса")
            .isCounted(isValid.toString())
            .expectedBehavior("Необходимая длительность фиксации положения корпуса (более %d°, но менее %d°) = %s сек".formatted(
                Math.round(element.bodyPostureCriteria().minDegree()),
                Math.round(element.bodyPostureCriteria().maxDegree()),
                DECIMAL_FORMAT.format(rulesProperties.balanceFixationDuration())
            ));
        var actualBehaviour = "Длительность фиксации положения корпуса гимнастки = %s сек. ".formatted(DECIMAL_FORMAT.format(duration));
        if (penalty > 0.0) {
            actualBehaviour += "Отклонение от необходимого положение = %d°".formatted(Math.round(angleDif));
        }
        return report
            .actualBehavior(actualBehaviour)
            .penalty(DECIMAL_FORMAT.format(penalty))
            .detectionQuality(Math.round(getDetectionQualityInPercentage(detectionQualityList)))
            .build();
    }

    private DetectionQualityState getDetectionQualityState(Coordinate supportLegCoordinate, Coordinate hipCoordinate, Coordinate shoulderCoordinate,
                                                           FrameInfo frameInfo, Double previousAngle, Double newAngle, Element.BodyPostureCriteria bodyPostureCriteria) {
        if (!isCoordinateInFrame(supportLegCoordinate, frameInfo) || !isCoordinateInFrame(shoulderCoordinate, frameInfo) || !isCoordinateInFrame(hipCoordinate, frameInfo)) {
            return DetectionQualityState.NOT_DETECTED;
        }
        var angleDif = previousAngle > 0 ? Math.abs(previousAngle - newAngle) : 0;

        // Вычисляем максимальную скорость движения корпуса (равновесие всегда начинается с положения корпуса = 180 град)
        var degreeVelocity = Math.abs(bodyPostureCriteria.maxDegree() - DEGREES_180) / frameInfo.fps();
        if (angleDif >= degreeVelocity) {
            return angleDif > 2 * degreeVelocity
                ? DetectionQualityState.EMISSION
                : DetectionQualityState.POSSIBLE_EMISSION;
        }
        return DetectionQualityState.GOOD;
    }
}
