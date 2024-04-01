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
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.PenaltyScorer;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.List;

import static ru.itis.rgjudge.db.enums.BodyPositionType.CLOSED;
import static ru.itis.rgjudge.db.enums.BodyPositionType.OPEN;
import static ru.itis.rgjudge.db.enums.TypeBySupportLeg.BACK;
import static ru.itis.rgjudge.db.enums.TypeBySupportLeg.FRONT;
import static ru.itis.rgjudge.db.enums.TypeBySupportLeg.SIDE;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_KNEE;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_ANKLE;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_KNEE;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAngle;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAverage;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;

@Component
@Order(1)
public class LegSplitEstimator implements Estimator {

    private static final Logger logger = LoggerFactory.getLogger(LegSplitEstimator.class);

    private final PenaltyScorer penaltyScorer;
    private final RulesProperties rulesProperties;

    public LegSplitEstimator(PenaltyScorer penaltyScorer, RulesProperties rulesProperties) {
        this.penaltyScorer = penaltyScorer;
        this.rulesProperties = rulesProperties;
    }

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.legDegreeCriteria().isActive();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseResponse.PoseData> poseData,
                                             List<BodyPart> bodyParts,
                                             Element element,
                                             BodyPositionType bodyPositionType) {
        var start = 0;
        var end = 0;
        var maxAngle = 0.0;
        var curAngle = 0.0;
        for (int i = 0; i < poseData.size(); i++) {
            var coordinates = poseData.get(i).getCoordinates();
            PoseResponse.Coordinate supportLegCoordinate;
            PoseResponse.Coordinate freeLegCoordinate;
            if (TypeBySupportLeg.BACK.equals(element.typeBySupportLeg())) {
                // Если тип элемента = заднее, вычисляем по голени, бедру и колену
                supportLegCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_ANKLE);
                freeLegCoordinate = getCoordinate(coordinates, bodyParts, LEFT_KNEE);
            } else {
                // Если тип элемента = переднее/боковое, вычисляем по колену, бедру и колену
                supportLegCoordinate = getCoordinate(coordinates, bodyParts, LEFT_KNEE);
                freeLegCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_KNEE);
            }

            var hipCoordinate = calculateAverage(
                getCoordinate(coordinates, bodyParts, LEFT_HIP),
                getCoordinate(coordinates, bodyParts, RIGHT_HIP)
            );

            curAngle = calculateLegSplitAngle(supportLegCoordinate, hipCoordinate, freeLegCoordinate, curAngle, bodyPositionType, element.typeBySupportLeg());

            // Вычисляем максимально достигнутый угол
            if (curAngle > maxAngle) maxAngle = curAngle;

            // Если ноги находятся в нужном положении, начинаем отсчет
            // Если нет и отсчет уже был начат - необходимое положение потеряно, прекращаем отсчет, прекращаем обработку
            if (curAngle >= element.legDegreeCriteria().minDegree() && curAngle <= element.legDegreeCriteria().maxDegree()) {
                end = i;
                if (start == 0) start = i;
            } else if (start != 0) break;
        }
        logger.info("Leg split estimator: element execution start frame = " + start + ", end frame = " + end);
        var duration = poseData.get(end).getTime() - poseData.get(start).getTime();

        // Элемент засчитан, если длительность фиксации положения ног не менее необходимого
        var isValid = duration - rulesProperties.balanceFixationDuration() > 0;

        // Применяем сбавку за отсутствие достижения нужного положения ног на протяжении всего элемента
        var penalty = NO_PENALTY;
        var angleDif = 0.0;
        if (!isValid && maxAngle < element.legDegreeCriteria().minDegree()) {
            angleDif = element.legDegreeCriteria().minDegree() - maxAngle;
            penalty = penaltyScorer.getPenaltyByAngle(angleDif);
        }

        return EstimatorResponse.builder()
            .isValid(isValid)
            .penalty(penalty)
            .elementExecutionPoseData(poseData.subList(start, end))
            .estimationType(EstimationType.FULL_CHECK)
            .reportData(prepareReportData(element, duration, penalty, angleDif, maxAngle, isValid))
            .build();
    }

    // Если равновесие заднее и положение закрытое, то угол превышает 180 градусов, когда стопа правее бедра, если открытое - наоборот, левее
    // С передним равновесием наоборот
    // Но в большинстве случаев будем вычислять по 1 варианту (точка выше прямой)
    private Double calculateLegSplitAngle(PoseResponse.Coordinate supportLegCoordinate,
                                          PoseResponse.Coordinate hipCoordinate,
                                          PoseResponse.Coordinate freeLegCoordinate,
                                          Double previousAngle,
                                          BodyPositionType bodyPositionType,
                                          TypeBySupportLeg typeBySupportLeg) {
        return OPEN.equals(bodyPositionType) && (FRONT.equals(typeBySupportLeg) || SIDE.equals(typeBySupportLeg))
            || CLOSED.equals(bodyPositionType) && BACK.equals(typeBySupportLeg)
            ? calculateAngle(supportLegCoordinate, hipCoordinate, freeLegCoordinate, previousAngle, (c2, c3) -> c3.getX() >= c2.getX())
            : calculateAngle(supportLegCoordinate, hipCoordinate, freeLegCoordinate, previousAngle, (c2, c3) -> c3.getX() <= c2.getX());
    }

    private ReportData prepareReportData(Element element, Double duration, Double penalty, Double angleDif, Double maxAngle, Boolean isValid) {
        var report = ReportData.builder()
            .estimatorName("Оценка шпагата")
            .isCounted(isValid)
            .expectedBehavior("Необходимая длительность фиксации положения ног (более %d°, но менее %d°) = %s сек".formatted(
                Math.round(element.legDegreeCriteria().minDegree()),
                Math.round(element.legDegreeCriteria().maxDegree()),
                DECIMAL_FORMAT.format(rulesProperties.balanceFixationDuration())
            ));
        var actualBehaviour = "Длительность фиксации положения ног гимнастки = %s сек. Максимальный угол в шпагате = %d°"
            .formatted(DECIMAL_FORMAT.format(duration), Math.round(maxAngle));
        if (penalty > 0.0) {
            actualBehaviour += "Отклонение от необходимого положение = %d°".formatted(Math.round(angleDif));
        }
        return report
            .actualBehavior(actualBehaviour)
            .penalty(DECIMAL_FORMAT.format(penalty))
            .build();
    }
}
