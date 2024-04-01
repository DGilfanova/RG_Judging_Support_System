package ru.itis.rgjudge.service.estimator.impl;

import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.LegPositionType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.PenaltyScorer;

import java.util.List;

import static ru.itis.rgjudge.db.enums.Side.LEFT;
import static ru.itis.rgjudge.dto.enums.BodyPart.getAnkle;
import static ru.itis.rgjudge.dto.enums.BodyPart.getHip;
import static ru.itis.rgjudge.dto.enums.BodyPart.getKnee;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.DEGREES_180;
import static ru.itis.rgjudge.utils.Constant.DEGREES_90;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;
import static ru.itis.rgjudge.utils.CoordinateUtils.calculateAngle;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;

public class BaseLegPositionEstimator {

    private static final double DEGREE_ACCURACY_ERROR = 13.0;

    protected final PenaltyScorer penaltyScorer;
    protected final RulesProperties rulesProperties;

    protected BaseLegPositionEstimator(PenaltyScorer penaltyScorer, RulesProperties rulesProperties) {
        this.penaltyScorer = penaltyScorer;
        this.rulesProperties = rulesProperties;
    }

    protected EstimatorResponse estimate(List<PoseResponse.PoseData> poseData, List<BodyPart> bodyParts, Element element, Side side) {
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
        var isCorrect = true;
        var errorAngle = 0.0;
        for (PoseResponse.PoseData poseDatum : poseData) {
            var coordinates = poseDatum.getCoordinates();
            var ankleCoordinate = getCoordinate(coordinates, bodyParts, getAnkle(side));
            var kneeCoordinate = getCoordinate(coordinates, bodyParts, getKnee(side));
            var hipCoordinate = getCoordinate(coordinates, bodyParts, getHip(side));

            // TODO: понизить DEGREE_ACCURACY_ERROR
            curAngle = calculateLegPositionAngle(curAngle, ankleCoordinate, kneeCoordinate, hipCoordinate);

            // В зависимости от типа "согнутоности" колена разная обработка
            switch (legPosition) {
                case BENT -> {
                    if (curAngle < DEGREES_180 - DEGREE_ACCURACY_ERROR) {
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
                    if (DEGREES_180 - curAngle > DEGREE_ACCURACY_ERROR && (errorAngle == 0.0 || curAngle < errorAngle)) {
                        isCorrect = false;
                        errorAngle = curAngle;
                    }
                }
            }
        }

        // Элемент не засчитан, если при выполнении была допущена ошибка (isCorrect == false) и estimationType != ONLY_PENALTY
        // Применяется сбавка, если при выполнении была допущена ошибка (isCorrect == false) и estimationType != ONLY_VALIDNESS
        var penalty = NO_PENALTY;
        var isValid = true;
        switch (estimationType) {
            case ONLY_PENALTY -> penalty = isCorrect ? NO_PENALTY : penaltyScorer.getPenaltyForBentBodyPart();
            case ONLY_VALIDNESS -> isValid = isCorrect;
            case FULL_CHECK -> {
                penalty = isCorrect ? NO_PENALTY : penaltyScorer.getPenaltyForBentBodyPart();
                isValid = isCorrect;
            }
        }

        return EstimatorResponse.builder()
            .isValid(isValid)
            .penalty(penalty)
            .reportData(prepareReportData(legPosition, penalty, errorAngle, isValid, side))
            .estimationType(estimationType)
            .build();
    }

    private Double calculateLegPositionAngle(Double previousAngle, PoseResponse.Coordinate ankleCoordinate,
                                             PoseResponse.Coordinate kneeCoordinate, PoseResponse.Coordinate hipCoordinate) {
        return calculateAngle(ankleCoordinate, kneeCoordinate, hipCoordinate);
    }

    private ReportData prepareReportData(LegPositionType legPositionType, Double penalty, Double errorAngle,
                                         Boolean isValid, Side side) {
        var report = ReportData.builder()
            .estimatorName(LEFT.equals(side) ? "Оценка положения левой ноги" : "Оценка положения правой ноги")
            .isCounted(isValid)
            .penalty(DECIMAL_FORMAT.format(penalty));

        switch (legPositionType) {
            case BENT -> report.expectedBehavior("Нога находится в согнутом положении (угол в колене менее 180°)")
                .actualBehavior(
                    isValid
                        ? "Нога согнута, угол = %d°".formatted(Math.round(errorAngle))
                        : "Нога не согнута, угол в колене = %d° (близок к 180°)".formatted(Math.round(errorAngle))
                );
            case BENT_LESS_90 -> report.expectedBehavior("Нога согнута (угол в колене менее 90°)")
                .actualBehavior(
                    isValid
                        ? "Нога в необходимом положении"
                        : "Нога согнута менее чем на 90°: угол = %d°".formatted(Math.round(errorAngle))
                );
            case BENT_MORE_90 -> report.expectedBehavior("Нога согнута (угол в колене более 90°)")
                .actualBehavior(
                    isValid
                        ? "Нога в необходимом положении"
                        : "Нога согнута более чем на 90°: угол = %d°".formatted(Math.round(errorAngle))
                );
            case STRAIGHT -> report.expectedBehavior("Нога находится в прямом положении (угол в колене близок к 180°)")
                .actualBehavior(
                    isValid
                        ? "Нога вытянута"
                        : "Нога согнута, угол в колене = %d°".formatted(Math.round(errorAngle))
                );
        }

        return report.build();
    }
}
