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
@Order(5)
public class RightLegPositionEstimator extends BaseLegPositionEstimator implements Estimator {

    public RightLegPositionEstimator(PenaltyScorer penaltyScorer, RulesProperties rulesProperties) {
        super(penaltyScorer, rulesProperties);
    }

    @Override
    public boolean isApplicableToElement(Element element) {
        return element.rightLegPositionCriteria().isActive();
    }

    @Override
    public EstimatorResponse estimateElement(List<PoseResponse.PoseData> poseData, List<BodyPart> bodyParts,
                                             Element element, BodyPositionType bodyPositionType) {
        return estimate(poseData, bodyParts, element, Side.RIGHT);
    }
}
