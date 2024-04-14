package ru.itis.rgjudge.dto.enums;

import ru.itis.rgjudge.db.enums.Side;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BodyPart {
    NOSE,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_HEEL,
    RIGHT_HEEL,
    LEFT_FOOT_INDEX,
    RIGHT_FOOT_INDEX;

    public static final Set<BodyPart> VALUES = Stream.of(values()).collect(Collectors.toUnmodifiableSet());

    public static List<BodyPart> getByStringList(List<String> stringBodyParts) {
        var bodyParts = stringBodyParts.stream()
                .map(BodyPart::valueOf)
                .toList();

        if (new HashSet<>(bodyParts).size() != VALUES.size()) {
            throw new IllegalStateException("Invalid list of body parts");
        }

        return bodyParts;
    }

    public static BodyPart getHip(Side side) {
        return switch (side) {
            case LEFT -> LEFT_HIP;
            case RIGHT -> RIGHT_HIP;
        };
    }

    public static BodyPart getAnkle(Side side) {
        return switch (side) {
            case LEFT -> LEFT_ANKLE;
            case RIGHT -> RIGHT_ANKLE;
        };
    }

    public static BodyPart getKnee(Side side) {
        return switch (side) {
            case LEFT -> LEFT_KNEE;
            case RIGHT -> RIGHT_KNEE;
        };
    }

    public static BodyPart getFootIndex(Side side) {
        return switch (side) {
            case LEFT -> LEFT_FOOT_INDEX;
            case RIGHT -> RIGHT_FOOT_INDEX;
        };
    }
}
