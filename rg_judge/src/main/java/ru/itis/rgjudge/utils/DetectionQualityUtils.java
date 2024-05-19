package ru.itis.rgjudge.utils;

import ru.itis.rgjudge.dto.enums.DetectionQualityState;

import java.util.List;

import static ru.itis.rgjudge.dto.enums.DetectionQualityState.MAX_STATE_VALUE;

public class DetectionQualityUtils {

    public static Double getDetectionQualityInPercentage(List<DetectionQualityState> states) {
        return (100.0 * states.stream().mapToInt(DetectionQualityState::getValue).sum() / (states.size() * MAX_STATE_VALUE));
    }
}
