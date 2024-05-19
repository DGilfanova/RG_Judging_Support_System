package ru.itis.rgjudge.service.estimator;

import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.FrameInfo;

import java.util.List;

public interface Estimator {

    boolean isApplicableToElement(Element element);

    EstimatorResponse estimateElement(List<PoseResponse.PoseData> poseData,
                                      List<BodyPart> bodyParts,
                                      Element element,
                                      BodyPositionType bodyPositionType,
                                      FrameInfo frameInfo,
                                      Side handed);
}
