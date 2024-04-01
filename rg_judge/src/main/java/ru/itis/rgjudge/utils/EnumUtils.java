package ru.itis.rgjudge.utils;

import org.apache.commons.lang3.StringUtils;

public class EnumUtils {

    public static <T extends Enum<T>> T getByName(Class<T> c, String enumValue) {
        if (StringUtils.isEmpty(enumValue)) {
            return null;
        }

        try {
            return Enum.valueOf(c, enumValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("No enum constant %s found by name = %s".formatted(c.getCanonicalName(), enumValue));
        }
    }
}
