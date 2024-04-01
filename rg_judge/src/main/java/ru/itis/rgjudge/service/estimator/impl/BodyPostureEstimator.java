package ru.itis.rgjudge.service.estimator.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.PenaltyScorer;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.List;

import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_ANKLE;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_SHOULDER;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_ANKLE;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_SHOULDER;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAngle;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAverage;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;

// TODO упомянуть в доке про обязательный порядок
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
    public EstimatorResponse estimateElement(List<PoseResponse.PoseData> poseData,
                                             List<BodyPart> bodyParts,
                                             Element element,
                                             BodyPositionType bodyPositionType) {
        var start = 0;
        var end = 0;
        var wasFixed = false;
        var closerAngleDif = 0.0;
        var curAngle = 0.0;
        for (int i = 0; i < poseData.size(); i++) {
            var coordinates = poseData.get(i).getCoordinates();
            var shoulderCoordinate = calculateAverage(
                getCoordinate(coordinates, bodyParts, LEFT_SHOULDER),
                getCoordinate(coordinates, bodyParts, RIGHT_SHOULDER)
            );
            PoseResponse.Coordinate supportLegCoordinate;
            PoseResponse.Coordinate hipCoordinate;
            if (TypeBySupportLeg.BACK.equals(element.typeBySupportLeg())) {
                // Если тип элемента = заднее, вычисляем по правым лодыжке и бедру
                supportLegCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_ANKLE);
                hipCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_HIP);
            } else {
                // Если тип элемента = переднее, вычисляем по левым колену и бедру
                supportLegCoordinate = getCoordinate(coordinates, bodyParts, LEFT_ANKLE);
                hipCoordinate = getCoordinate(coordinates, bodyParts, LEFT_HIP);
            }

            curAngle = calculateAngle(supportLegCoordinate, hipCoordinate, shoulderCoordinate, curAngle, bodyPositionType, element.typeBySupportLeg());

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
                if (currentAngleDif < closerAngleDif) closerAngleDif = currentAngleDif;
                if (start > 0) wasFixed = true;
            }
        }
        logger.info("Element execution start frame = " + start + ", end frame = " + end);
        var duration = poseData.get(end).getTime() - poseData.get(start).getTime();

        // Элемент засчитан, если длительность фиксации положения теля не менее необходимого
        var isValid = duration - rulesProperties.balanceFixationDuration() > 0;

        // Применяем сбавку за отсутствие достижения нужного положения тела на протяжении всего элемента
        var penalty = 0.0;
        if (!isValid) {
            penalty = penaltyScorer.getPenaltyByAngle(closerAngleDif);
        }

        return EstimatorResponse.builder()
            .isValid(isValid)
            .penalty(penalty)
            .elementExecutionPoseData(poseData.subList(start, end))
            .reportData(prepareReportData(element, duration, penalty, closerAngleDif, isValid))
            .estimationType(EstimationType.FULL_CHECK)
            .build();
    }

    private Double countCloserAngleDif(Double curAngle, Float minAngle, Float maxAngle) {
        return Math.min(Math.abs(minAngle - curAngle), Math.abs(maxAngle - curAngle));
    }

    private ReportData prepareReportData(Element element, Double duration, Double penalty, Double angleDif, Boolean isValid) {
        var report = ReportData.builder()
            .estimatorName("Оценка положения корпуса")
            .isCounted(isValid)
            .expectedBehavior("Необходимая длительность фиксации положения корпуса (более %d°, но менее %d°) = %s сек".formatted(
                Math.round(element.bodyPostureCriteria().minDegree()),
                Math.round(element.bodyPostureCriteria().maxDegree()),
                DECIMAL_FORMAT.format(rulesProperties.balanceFixationDuration())
            ));
        var actualBehaviour = "Длительность фиксации положения корпуса гимнастки = %s сек".formatted(DECIMAL_FORMAT.format(duration));
        if (penalty > 0.0) {
            actualBehaviour += "Отклонение от необходимого положение = %d°".formatted(Math.round(angleDif));
        }
        return report
            .actualBehavior(actualBehaviour)
            .penalty(DECIMAL_FORMAT.format(penalty))
            .build();
    }
}
