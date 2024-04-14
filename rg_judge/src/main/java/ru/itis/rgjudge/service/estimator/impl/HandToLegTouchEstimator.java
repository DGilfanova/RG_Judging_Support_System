package ru.itis.rgjudge.service.estimator.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse.Coordinate;
import ru.itis.rgjudge.dto.PoseResponse.PoseData;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.List;

import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_ELBOW;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_WRIST;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_ELBOW;
import static ru.itis.rgjudge.dto.enums.BodyPart.getFootIndex;
import static ru.itis.rgjudge.dto.enums.BodyPart.getKnee;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculate2DDistance;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateDistanceFromPointToLine;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;

@Component
@Order(6)
public class HandToLegTouchEstimator implements Estimator {

    private static final Double CALF_WIDTH_TO_SHIN_LENGTH_PROPORTION = 0.31;

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.handToLegTouchCriteria().isActive();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseData> poseData, List<BodyPart> bodyParts, Element element, BodyPositionType bodyPositionType) {
        var type = element.handToLegTouchCriteria().type();
        var grabbedLegSide = element.typeBySupportLeg().equals(TypeBySupportLeg.BACK) ? Side.LEFT : Side.RIGHT;

        var isCorrect = false;
        var thighWidth = 0.0;
        var calfWidth = 0.0;
        for (PoseData poseDatum : poseData) {
            var coordinates = poseDatum.getCoordinates();
            var footCoordinate = getCoordinate(coordinates, bodyParts, getFootIndex(grabbedLegSide));
            var kneeCoordinate = getCoordinate(coordinates, bodyParts, getKnee(grabbedLegSide));

            if (calfWidth == 0.0)
                calfWidth = CALF_WIDTH_TO_SHIN_LENGTH_PROPORTION * calculate2DDistance(footCoordinate, kneeCoordinate);

            var leftWristCoordinate = getCoordinate(coordinates, bodyParts, LEFT_WRIST);
            var rightWristCoordinate = getCoordinate(coordinates, bodyParts, LEFT_WRIST);

            // В зависимости от типа хвата ноги рукой разная обработка
            var legIsTouched = switch (type) {
                case HIGH_POSTURE -> {
                    var leftElbowCoordinate = getCoordinate(coordinates, bodyParts, LEFT_ELBOW);
                    var rightElbowCoordinate = getCoordinate(coordinates, bodyParts, RIGHT_ELBOW);

                    var isLeftGrabbedLeg = isForearmInArea(footCoordinate, kneeCoordinate, leftWristCoordinate, leftElbowCoordinate, calfWidth);
                    var isRightGrabbedLeg = isForearmInArea(footCoordinate, kneeCoordinate, rightWristCoordinate, rightElbowCoordinate, calfWidth);
                    yield isLeftGrabbedLeg || isRightGrabbedLeg;
                }
                case LOW_POSTURE -> {
                    var isLeftWristGrabbedLeg = isHandInArea(footCoordinate, kneeCoordinate, leftWristCoordinate, thighWidth);
                    var isRightWristGrabbedLeg = isHandInArea(footCoordinate, kneeCoordinate, rightWristCoordinate, thighWidth);
                    yield isLeftWristGrabbedLeg || isRightWristGrabbedLeg;
                }
            };
            var isCorrectState = element.handToLegTouchCriteria().isTouch() == legIsTouched;
            isCorrect = isCorrect || isCorrectState;
        }

        return EstimatorResponse.builder()
            .isValid(isCorrect)
            .penalty(NO_PENALTY)
            .reportData(prepareReportData(element, isCorrect))
            .estimationType(EstimationType.FULL_CHECK)
            .build();
    }

    // Проверяем, что checkedPoint находится между прямыми, и ее расстояние до голени меньше ее ширины пополам
    private boolean isHandInArea(Coordinate ankleCoordinate, Coordinate kneeCoordinate, Coordinate wristCoordinate, Double width) {
        return (isPointAboveLine(ankleCoordinate, kneeCoordinate, wristCoordinate) ^ isPointAboveLine(kneeCoordinate, ankleCoordinate, wristCoordinate))
            && calculateDistanceFromPointToLine(ankleCoordinate, kneeCoordinate, wristCoordinate) <= width / 2;
    }

    private boolean isForearmInArea(Coordinate ankleCoordinate, Coordinate kneeCoordinate, Coordinate wristCoordinate,
                                          Coordinate elbowCoordinate, Double width) {
        return isHandInArea(ankleCoordinate, kneeCoordinate, wristCoordinate, width)
            || isHandInArea(ankleCoordinate, kneeCoordinate, elbowCoordinate, width);
    }

    // Вычислить, находится ли точка c3 выше прямой, перпендикулярной прямой (с1;с2) и проходящей через точку с1
    private boolean isPointAboveLine(Coordinate c1, Coordinate c2, Coordinate c3) {
        double m = -1.0 / ((c2.getY() - c1.getY()) / (c2.getX() - c1.getX()));
        double b = c1.getY() - m * c1.getX();
        return c3.getY() > c3.getX() * m + b;
    }

    private ReportData prepareReportData(Element element, Boolean isCorrect) {
        var report = ReportData.builder()
            .estimatorName("Положение рук")
            .isCounted(isCorrect.toString())
            .expectedBehavior(element.handToLegTouchCriteria().isTouch()
                ? "Рука должна касаться ноги"
                : "Рука не должна касаться ноги"
            )
            .actualBehavior(element.handToLegTouchCriteria().isTouch() && isCorrect || !element.handToLegTouchCriteria().isTouch() && !isCorrect
                ? "Рука касалась ноги"
                : "Рука не касалась ноги"
            );
        return report.penalty(DECIMAL_FORMAT.format(NO_PENALTY))
            .build();
    }
}
