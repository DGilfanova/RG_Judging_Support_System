package ru.itis.rgjudge.db.model;

import lombok.Builder;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.HandToLegTouchType;
import ru.itis.rgjudge.db.enums.LegPositionType;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;

@Builder
public record Element (
        Integer id,
        String name,
        Double value,
        TypeBySupportLeg typeBySupportLeg,
        BodyPostureCriteria bodyPostureCriteria,
        LegDegreeCriteria legDegreeCriteria,
        LeftLegPositionCriteria leftLegPositionCriteria,
        RightLegPositionCriteria rightLegPositionCriteria,
        HandToLegTouchCriteria handToLegTouchCriteria
){

    @Builder
    public record BodyPostureCriteria(
            Float minDegree,
            Float maxDegree
    ) {

        public boolean isActive() {
            return minDegree != null || maxDegree != null;
        }
    }

    @Builder
    public record LegDegreeCriteria(
            Float minDegree,
            Float maxDegree
    ) {

        public boolean isActive() {
            return minDegree != null || maxDegree != null;
        }
    }

    @Builder
    public record LeftLegPositionCriteria(
            LegPositionType legPosition,
            EstimationType estimationType
    ) {

        public boolean isActive() {
            return legPosition != null;
        }
    }

    @Builder
    public record RightLegPositionCriteria(
        LegPositionType legPosition,
        EstimationType estimationType
    ) {

        public boolean isActive() {
            return legPosition != null;
        }
    }

    @Builder
    public record HandToLegTouchCriteria(
            HandToLegTouchType type,
            Boolean isTouch
    ) {

        public boolean isActive() {
            return type != null;
        }
    }

}
