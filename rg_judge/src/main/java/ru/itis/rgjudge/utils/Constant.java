package ru.itis.rgjudge.utils;

import java.text.DecimalFormat;

public class Constant {

    // Video file properties
    public static final Integer MB_IN_BYTES = 1024 * 1024;

    // Degrees
    public static final double DEGREES_360 = 360.0;
    public static final double DEGREES_180 = 180.0;
    public static final double DEGREES_90 = 90.0;
    public static final double DEGREES_45 = 45.0;
    public static final double ANGLE_ACCURACY = 1;
    public static final double DEGREE_ACCURACY = 10.0;

    // Score
    public static final double ZERO_SCORE = 0.0;
    public static final double NO_PENALTY = 0.0;
    public static final String IGNORE_ESTIMATION = "ignore";

    // Format
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    // Error messages
    public static final String INVALID_VIDEO_FILE_SIZE_ERROR = "Video file size = %s MB exceed max = %s MB. Please upload another one";
}
