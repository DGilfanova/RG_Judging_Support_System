package ru.itis.rgjudge.service.estimator.impl;

import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.LegPositionType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse.Coordinate;
import ru.itis.rgjudge.dto.PoseResponse.PoseData;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.enums.Criteria;
import ru.itis.rgjudge.dto.enums.DetectionQualityState;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.FrameInfo;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.PenaltyScorer;

import java.util.ArrayList;
import java.util.List;

import static ru.itis.rgjudge.db.enums.Side.LEFT;
import static ru.itis.rgjudge.dto.enums.BodyPart.getAnkle;
import static ru.itis.rgjudge.dto.enums.BodyPart.getHip;
import static ru.itis.rgjudge.dto.enums.BodyPart.getKnee;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.DEGREES_180;
import static ru.itis.rgjudge.utils.Constant.DEGREES_90;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.Constant.CONTROVERSIAL_SITUATION_PROBABILITY_ACCURACY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAngle;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;
import static ru.itis.rgjudge.utils.CoordinateUtils.isCoordinateInFrame;
import static ru.itis.rgjudge.utils.DetectionQualityUtils.getDetectionQualityInPercentage;

public class BaseLegPositionEstimator {

    private static final double MAX_DEGREE_ACCURACY = 19.0;
    private static final double DEGREE_ACCURACY = 5.0;

    protected final PenaltyScorer penaltyScorer;
    protected final RulesProperties rulesProperties;

    protected BaseLegPositionEstimator(PenaltyScorer penaltyScorer, RulesProperties rulesProperties) {
        this.penaltyScorer = penaltyScorer;
        this.rulesProperties = rulesProperties;
    }

    protected EstimatorResponse estimate(List<PoseData> poseData, List<BodyPart> bodyParts, Element element, Side side, FrameInfo frameInfo, Side handed) {
        LegPositionType legPosition;
        EstimationType estimationType;
        if (LEFT.equals(side)) {
            legPosition = element.leftLegPositionCriteria().legPosition();
            estimationType = element.leftLegPositionCriteria().estimationType();
        } else {
            legPosition = element.rightLegPositionCriteria().legPosition();
            estimationType = element.rightLegPositionCriteria().estimationType();
        }

        var curAngle = 0.0;
        var errorAngle = 0.0;
        var start = 0;
        var end = 0;
        var wasFixed = false;

        var isControversialSituation = false;
        var detectionQualityList = new ArrayList<DetectionQualityState>();

        for (int i = 0; i < poseData.size(); i++) {
            var coordinates = poseData.get(i).getCoordinates();
            var isCorrect = true;

            var ankleCoordinate = getCoordinate(coordinates, bodyParts, getAnkle(side));
            var kneeCoordinate = getCoordinate(coordinates, bodyParts, getKnee(side));
            var hipCoordinate = getCoordinate(coordinates, bodyParts, getHip(side));

            var newAngle = calculateAngle(ankleCoordinate, kneeCoordinate, hipCoordinate);
            detectionQualityList.add(getDetectionQualityState(ankleCoordinate, kneeCoordinate, hipCoordinate, frameInfo, curAngle, newAngle));
            curAngle = newAngle;

            // В зависимости от типа "согнутости" колена разная обработка
            switch (legPosition) {
                case BENT -> {
                    if (curAngle < DEGREES_180 - MAX_DEGREE_ACCURACY) {
                        if (errorAngle == 0.0 || Math.abs(DEGREES_180 - curAngle) < errorAngle) errorAngle = curAngle;
                        isCorrect = false;
                    }
                }
                case BENT_LESS_90 -> {
                    if (curAngle >= DEGREES_90 && (errorAngle == 0.0 || curAngle > errorAngle)) {
                        isCorrect = false;
                        errorAngle = curAngle;
                    }
                }
                case BENT_MORE_90 -> {
                    if (curAngle <= DEGREES_90 && (errorAngle == 0.0 || curAngle < errorAngle)) {
                        isCorrect = false;
                        errorAngle = curAngle;
                    }
                }
                case STRAIGHT -> {
                    if (DEGREES_180 - curAngle > MAX_DEGREE_ACCURACY && (errorAngle == 0.0 || curAngle < errorAngle)) {
                        isCorrect = false;
                        errorAngle = curAngle;
                    }
                    // Попадание в зону большой погрешности, уменьшаем вероятность правильного ответа
                    if (DEGREES_180 - curAngle > DEGREE_ACCURACY && (errorAngle == 0.0 || curAngle < errorAngle)) {
                        isControversialSituation = true;
                    }
                }
            }
            if (isCorrect) {
                if (start == 0) start = i;
            } else {
                if (start != 0 && !wasFixed) {
                    end = i;
                    wasFixed = true;
                }
            }
        }

        // Элемент не засчитан, если при выполнении была допущена ошибка (isCorrect == false) и estimationType != ONLY_PENALTY
        // Применяется сбавка, если при выполнении была допущена ошибка (isCorrect == false) и estimationType != ONLY_VALIDNESS
        var penalty = NO_PENALTY;
        var duration = start > 0 ? poseData.get(end > 0 ? end : poseData.size() - 1).getTime() - poseData.get(start).getTime() : 0.0;
        var isValid = duration - rulesProperties.balanceFixationDuration() > 0;

        switch (estimationType) {
            case ONLY_PENALTY -> {
                penalty = isValid ? NO_PENALTY : penaltyScorer.getPenaltyForBentBodyPart();
                isValid = true;
            }
            case FULL_CHECK -> penalty = isValid ? NO_PENALTY : penaltyScorer.getPenaltyForBentBodyPart();
        }

        return EstimatorResponse.builder()
            .criteria(Criteria.LEG_POSITION)
            .isValid(isValid)
            .penalty(penalty)
            .reportData(prepareReportData(legPosition, penalty, errorAngle, isValid, side, detectionQualityList, isControversialSituation))
            .estimationType(estimationType)
            .build();
    }

    private ReportData prepareReportData(LegPositionType legPositionType, Double penalty, Double errorAngle,
                                         Boolean isValid, Side side, List<DetectionQualityState> detectionQualityList,
                                         boolean isControversialSituation) {
        var report = ReportData.builder()
            .estimatorName(LEFT.equals(side) ? "Оценка положения левой ноги" : "Оценка положения правой ноги")
            .isCounted(isValid.toString())
            .penalty(DECIMAL_FORMAT.format(penalty));

        switch (legPositionType) {
            case BENT -> report.expectedBehavior("Нога находится в согнутом положении (угол в колене менее 180°)")
                .actualBehavior(
                    isValid && penalty == NO_PENALTY
                        ? "Нога согнута, угол = %d°".formatted(Math.round(errorAngle))
                        : "Нога не согнута, угол в колене = %d° (близок к 180°)".formatted(Math.round(errorAngle))
                );
            case BENT_LESS_90 -> report.expectedBehavior("Нога согнута (угол в колене менее 90°)")
                .actualBehavior(
                    isValid && penalty == NO_PENALTY
                        ? "Нога в необходимом положении"
                        : "Нога согнута менее чем на 90°: угол = %d°".formatted(Math.round(errorAngle))
                );
            case BENT_MORE_90 -> report.expectedBehavior("Нога согнута (угол в колене более 90°)")
                .actualBehavior(
                    isValid && penalty == NO_PENALTY
                        ? "Нога в необходимом положении"
                        : "Нога согнута более чем на 90°: угол = %d°".formatted(Math.round(errorAngle))
                );
            case STRAIGHT -> report.expectedBehavior("Нога находится в прямом положении (угол в колене близок к 180°)")
                .actualBehavior(
                    isValid && penalty == NO_PENALTY
                        ? "Нога вытянута"
                        : "Нога согнута, угол в колене = %d°".formatted(Math.round(errorAngle))
                );
        }

        double detectionQuality = getDetectionQualityInPercentage(detectionQualityList);
        if (isControversialSituation) {
            detectionQuality *= CONTROVERSIAL_SITUATION_PROBABILITY_ACCURACY;
        }

        return report
            .detectionQuality(Math.round(detectionQuality))
            .build();
    }

    private DetectionQualityState getDetectionQualityState(Coordinate ankleCoordinate, Coordinate kneeCoordinate, Coordinate hipCoordinate,
                                                           FrameInfo frameInfo, Double previousAngle, Double newAngle) {
        if (!isCoordinateInFrame(ankleCoordinate, frameInfo) || !isCoordinateInFrame(kneeCoordinate, frameInfo) || !isCoordinateInFrame(hipCoordinate, frameInfo)) {
            return DetectionQualityState.NOT_DETECTED;
        }
        var angleDif = Math.abs(previousAngle - newAngle);

        // Вычисляем максимальную скорость движения ноги (насколько сильно может измениться градус за кадр)
        // Если превышает максимальное значение - возможный выброс, если дважды превышает - точно выброс
        var degreeVelocity = DEGREES_180 / frameInfo.fps();
        if (angleDif >= degreeVelocity) {
            return angleDif > 2 * degreeVelocity
                ? DetectionQualityState.EMISSION
                : DetectionQualityState.POSSIBLE_EMISSION;
        }
        return DetectionQualityState.GOOD;
    }
}
