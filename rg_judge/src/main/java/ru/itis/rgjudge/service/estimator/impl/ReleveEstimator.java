package ru.itis.rgjudge.service.estimator.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse.PoseData;
import ru.itis.rgjudge.dto.PoseResponse.Coordinate;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.PenaltyScorer;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.List;

import static ru.itis.rgjudge.db.enums.BodyPositionType.OPEN;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_FOOT_INDEX;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_HEEL;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_FOOT_INDEX;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_HEEL;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.DEGREES_180;
import static ru.itis.rgjudge.utils.Constant.DEGREE_ACCURACY;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculate3DAngle;
import static ru.itis.rgjudge.utils.CoordinateUtils.checkNotDataEmission;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;

@Component
@Order(3)
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
        // TODO после добавления прыжков: если равновесие и не сидячее
        return element.value() > penaltyScorer.getPenaltyForNotReleve();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseData> poseData, List<BodyPart> bodyParts, Element element, BodyPositionType bodyPositionType) {
        var start = 0;
        var end = 0;
        var curAngle = 0.0;
        var maxAngle = 0.0;
        var isReleve = true;
        var isReleveRegistered = false;
        for (int i = 0; i < poseData.size(); i++) {
            var coordinates = poseData.get(i).getCoordinates();

            curAngle = switch (element.typeBySupportLeg()) {
                case BACK -> {
                    var rightHeelCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_HEEL);
                    var rightFootIndexCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_FOOT_INDEX);

                    yield calculateAngle(rightHeelCoordinate, rightFootIndexCoordinate, curAngle, bodyPositionType);
                }
                case FRONT, SIDE -> {
                    var leftHeelCoordinate = getCoordinate(coordinates, bodyParts, LEFT_HEEL);
                    var leftFootIndexCoordinate = getCoordinate(coordinates, bodyParts, LEFT_FOOT_INDEX);

                    yield calculateAngle(leftHeelCoordinate, leftFootIndexCoordinate, curAngle, bodyPositionType);
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
        var isValid = isReleveRegistered && duration - rulesProperties.balanceFixationDuration() > 0;

        var penalty = NO_PENALTY;
        if (!isReleveRegistered) penalty = penaltyScorer.getPenaltyForNotReleve();
        // TODO invalid logic
        if (!isValid) penalty = penaltyScorer.getPenaltyForFallingFromReleve();

        return EstimatorResponse.builder()
                .isValid(Boolean.TRUE)
                .penalty(penalty)
                .reportData(prepareReportData(isValid, isReleveRegistered, penalty, maxAngle, duration))
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
        var newAngle = OPEN.equals(bodyPositionType)
                ? c1.getX() <= c2.getX() && checkNotDataEmission(DEGREES_180 - angle, previousAngle) ? DEGREES_180 - angle : angle
                : c1.getX() >= c2.getX() && checkNotDataEmission(DEGREES_180 - angle, previousAngle) ? DEGREES_180 - angle : angle;

        // Для определения точек стопы требуется большая точность, именно здесь возможны наибольшие выбросы
        return Math.abs(previousAngle - newAngle) <= DEGREE_ACCURACY ? newAngle : previousAngle;
    }

    private ReportData prepareReportData(Boolean isValid, Boolean isReleveRegistered, Double penalty, Double maxAngle, Double duration) {
        var report = ReportData.builder()
                .isCounted(Boolean.TRUE)
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
                .build();
    }
}
