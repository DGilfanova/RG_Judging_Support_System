package ru.itis.rgjudge.service.impl;

import org.springframework.stereotype.Component;
import ru.itis.rgjudge.config.properties.PenaltyProperties;
import ru.itis.rgjudge.service.PenaltyScorer;

@Component
public class PenaltyScorerImpl implements PenaltyScorer {

    private final PenaltyProperties penaltyProperties;

    public PenaltyScorerImpl(PenaltyProperties penaltyProperties) {
        this.penaltyProperties = penaltyProperties;
    }

    @Override
    public Double getPenaltyByAngle(Double deflectionAngle) {
        if (deflectionAngle <= penaltyProperties.standardDeviation().smallDeviation()) return  penaltyProperties.standardDeviation().lessThanSmallDeviation();
        if (deflectionAngle <= penaltyProperties.standardDeviation().largeDeviation()) return  penaltyProperties.standardDeviation().lessThanLargeDeviation();
        return  penaltyProperties.standardDeviation().moreThanLargeDeviation();
    }

    @Override
    public Double getPenaltyForBentBodyPart() {
        return penaltyProperties.bentBodyPart();
    }

    @Override
    public Double getPenaltyForNotReleve() {
        return penaltyProperties.notReleve();
    }

    @Override
    public Double getPenaltyForFallingFromReleve() {
        return penaltyProperties.fallFromReleve();
    }
}
