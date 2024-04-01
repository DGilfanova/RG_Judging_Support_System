package ru.itis.rgjudge.service.estimator.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.service.PenaltyScorer;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.util.List;

@Component
@Order(4)
public class LeftLegPositionEstimator extends BaseLegPositionEstimator implements Estimator {

    public LeftLegPositionEstimator(PenaltyScorer penaltyScorer, RulesProperties rulesProperties) {
        super(penaltyScorer, rulesProperties);
    }

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.leftLegPositionCriteria().isActive();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseResponse.PoseData> poseData, List<BodyPart> bodyParts,
                                             Element element, BodyPositionType bodyPositionType) {
        return estimate(poseData, bodyParts, element, Side.LEFT);
    }
}
