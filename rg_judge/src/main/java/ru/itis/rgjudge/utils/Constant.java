package ru.itis.rgjudge.utils;

import java.text.DecimalFormat;

public class Constant {

    // Video file properties
    public static final Integer MB_IN_BYTES = 1024 * 1024;

    // Degrees
    public static final double DEGREES_360 = 360.0;
    public static final double DEGREES_270 = 270.0;
    public static final double DEGREES_180 = 180.0;
    public static final double DEGREES_90 = 90.0;

    // Accuracy
    public static final double ANGLE_ACCURACY = 3;
    public static final double DEGREE_ACCURACY = 20.0;
    public static final double CONTROVERSIAL_SITUATION_PROBABILITY_ACCURACY = 0.5;

    // Score
    public static final double ZERO_SCORE = 0.0;
    public static final double NO_PENALTY = 0.0;

    // Format
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    // Error messages

    // EN: Video file size = %s MB exceed max = %s MB. Please upload another one
    public static final String INVALID_VIDEO_FILE_SIZE_ERROR = "Размер видео = %s MB превышает максимальное значение = %s MB. Пожалуйста, загрузите другое видео.";
    // EN: Video fps = %s but min = %s. Please upload higher quality video
    public static final String INVALID_VIDEO_FPS_ERROR = "Fps видео = %s, минимальное разрешенное = %s. Пожалуйста, загрузите видео более высокого качества.";
    // EN: Gymnast wasn't detected on the video. Please upload higher quality video
    public static final String UNDETECTED_HUMAN_ERROR = "Спортсмен не был обнаружен на видео. Пожалуйста, загрузите видео более высокого качества.";
    public static final String BAD_DETECTION_QUALITY_ERROR = "Пожалуйста, загрузите видео более высокого качества. Спортсмен был обнаружен частично";
}
