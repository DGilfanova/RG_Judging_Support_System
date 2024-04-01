package ru.itis.rgjudge.utils;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.BodyPart;

import java.util.List;

import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.LEFT_SHOULDER;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_HIP;
import static ru.itis.rgjudge.dto.enums.BodyPart.RIGHT_SHOULDER;
import static ru.itis.rgjudge.utils.CoordinateUtils.getCoordinate;

@UtilityClass
public class BodyPostureUtils {

    private static final Logger logger = LoggerFactory.getLogger(BodyPostureUtils.class);

    public static BodyPositionType getBodyPositionType(List<PoseResponse.PoseData> poseData,
                                                             List<BodyPart> bodyParts,
                                                             TypeBySupportLeg typeBySupportLeg,
                                                             Integer frameCount) {
        //После теста определено, что для SIDE логика открытости/закрытости та же, но временно оставляю всегда открое положение
        if (TypeBySupportLeg.SIDE.equals(typeBySupportLeg)) {
            return BodyPositionType.OPEN;
        }

        int firstElemPoint = getAverageStartElementPoint(frameCount);
        int secondElemPoint = getAverageEndElementPoint(frameCount);
        var firstPointCoordinates = poseData.get(firstElemPoint).getCoordinates();
        var secondPointCoordinates = poseData.get(secondElemPoint).getCoordinates();

        var isLeftShoulderFPCloser = getCoordinate(firstPointCoordinates, bodyParts, LEFT_SHOULDER).getZ()
                < getCoordinate(firstPointCoordinates, bodyParts, RIGHT_SHOULDER).getZ();
        var isLeftHipFPCloser = getCoordinate(firstPointCoordinates, bodyParts, LEFT_HIP).getZ()
                < getCoordinate(firstPointCoordinates, bodyParts, RIGHT_HIP).getZ();
        var isLeftShoulderSPCloser = getCoordinate(secondPointCoordinates, bodyParts, LEFT_SHOULDER).getZ()
                < getCoordinate(firstPointCoordinates, bodyParts, RIGHT_SHOULDER).getZ();
        var isLeftHipSPCloser = getCoordinate(secondPointCoordinates, bodyParts, LEFT_HIP).getZ()
                < getCoordinate(firstPointCoordinates, bodyParts, RIGHT_HIP).getZ();

        var isFPCorrect = isLeftShoulderFPCloser == isLeftHipFPCloser;
        var isSPCorrect = isLeftShoulderSPCloser == isLeftHipSPCloser;

        if (isFPCorrect) {
            return isLeftShoulderFPCloser ? BodyPositionType.OPEN : BodyPositionType.CLOSED;
        }
        if (isSPCorrect) {
            return isLeftShoulderSPCloser ? BodyPositionType.OPEN : BodyPositionType.CLOSED;
        }

        logger.warn("Can not to define open/closed body position type. Use shoulder position in {} frame for definition", firstElemPoint);
        return isLeftShoulderFPCloser ? BodyPositionType.OPEN : BodyPositionType.CLOSED;
    }

    // Experimentally calculated frame of the beginning of the element on average
    private static int getAverageStartElementPoint(int frameCount) {
        return frameCount * 4/9;
    }

    // Experimentally calculated frame of the ending of the element on average
    private static int getAverageEndElementPoint(int frameCount) {
        return frameCount * 5/9;
    }
}
