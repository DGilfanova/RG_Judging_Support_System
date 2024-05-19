package ru.itis.rgjudge.dto.enums;

public enum DetectionQualityState {
    GOOD(3),
    NOT_DETECTED(0),
    EMISSION(1),
    POSSIBLE_EMISSION(2);

    public static final Integer MAX_STATE_VALUE = GOOD.value;
    private final Integer value;

    DetectionQualityState(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static DetectionQualityState getStateByValue(Integer value) {
        for (DetectionQualityState state: values()) {
            if (value.equals(state.value)) {
                return state;
            }
        }
        throw new IllegalStateException("Can't find state by value = %s".formatted(value));
    }
}
