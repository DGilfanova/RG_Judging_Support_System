package ru.itis.rgjudge.utils;

import lombok.experimental.UtilityClass;
import ru.itis.rgjudge.db.enums.BodyPositionType;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.dto.PoseResponse.Coordinate;
import ru.itis.rgjudge.dto.enums.BodyPart;

import java.util.List;
import java.util.function.BiPredicate;

import static ru.itis.rgjudge.db.enums.BodyPositionType.OPEN;
import static ru.itis.rgjudge.utils.Constant.DEGREES_360;
import static ru.itis.rgjudge.utils.Constant.ANGLE_ACCURACY;
import static ru.itis.rgjudge.utils.Constant.DEGREE_ACCURACY;

@UtilityClass
public class CoordinateUtils {

    // Для вычисления угла между (b, a) и (b, c) в 3D. Итоговое значение угла от 0 до 180
    public static Double calculate3DAngle(Coordinate a, Coordinate b, Coordinate c) {
        double[] ba = {a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ()};
        double[] bc = {c.getX() - b.getX(), c.getY() - b.getY(), c.getZ() - b.getZ()};

        double lengthAb = Math.sqrt(ba[0] * ba[0] + ba[1] * ba[1] + ba[2] * ba[2]);
        double lengthBc = Math.sqrt(bc[0] * bc[0] + bc[1] * bc[1] + bc[2] * bc[2]);

        double dotProduct = ba[0] * bc[0] + ba[1] * bc[1] + ba[2] * bc[2];

        double cosTheta = dotProduct / (lengthAb * lengthBc);

        double angleRadians = Math.acos(cosTheta);

        return Math.toDegrees(angleRadians);
    }

    // Для вычисления угла между (с2, c1) и (c2, c3). Итоговое значение угла от 0 до 180
    public static Double calculateAngle(Coordinate c1, Coordinate c2, Coordinate c3) {
        double v1x = c1.getX() - c2.getX();
        double v1y = c1.getY() - c2.getY();
        double v2x = c3.getX() - c2.getX();
        double v2y = c3.getY() - c2.getY();

        double dotProduct = v1x * v2x + v1y * v2y;

        double v1Length = Math.sqrt(v1x * v1x + v1y * v1y);
        double v2Length = Math.sqrt(v2x * v2x + v2y * v2y);

        return Math.toDegrees(Math.acos(dotProduct / (v1Length * v2Length)));
    }

    // Первый вариант определения degree или 360-degree (точка находится выше прямой или нет).
    public static Double calculateAngle(Coordinate c1, Coordinate c2, Coordinate c3, Double previousAngle) {
        double degree = calculateAngle(c1, c2, c3);
        return isPointAboveLine(c1, c2, c3) && checkNotDataEmission(DEGREES_360 - degree, previousAngle) ? DEGREES_360 - degree : degree;
    }

    /* Второй вариант: определения degree или 360-degree
      Определение происходит через открытое/закрытое положение тела при выполнении
      Если равновесие заднее и положение закрытое, то угол превышает 180 градусов, когда стопа правее бедра, если открытое - наоборот, левее
      С передним равновесием наоборот
    */
    public static Double calculateAngle(Coordinate c1, Coordinate c2, Coordinate c3, Double previousAngle,
                                        BodyPositionType bodyPositionType, TypeBySupportLeg typeBySupportLeg) {
        double angle = calculateAngle(c1, c2, c3);
        return OPEN.equals(bodyPositionType)
                ? c3.getX() >= c2.getX() && checkNotDataEmission(DEGREES_360 - angle, previousAngle) ? DEGREES_360 - angle : angle
                : c3.getX() <= c2.getX() && checkNotDataEmission(DEGREES_360 - angle, previousAngle) ? DEGREES_360 - angle : angle;
    }

    // Комбо вариант: определения degree или 360-degree (1 + 2), 2 применяется, когда прямая опорной ноги почти параллельна оси ординат
    public static Double calculateAngle(Coordinate c1, Coordinate c2, Coordinate c3, Double previousAngle,
                                        BiPredicate<Coordinate, Coordinate> over180Condition) {
        double angle = calculateAngle(c1, c2, c3);
        if (isAlmostParallelToYAxis(c1, c2)) {
            return over180Condition.test(c2, c3) && checkNotDataEmission(DEGREES_360 - angle, previousAngle) ? DEGREES_360 - angle : angle;
        } else {
            return isPointAboveLine(c1, c2, c3) && checkNotDataEmission(DEGREES_360 - angle, previousAngle) ? DEGREES_360 - angle : angle;
        }
    }

    // Вычислить, находится ли точка c3 выше прямой (c1; c2)
    public static boolean isPointAboveLine(Coordinate c1, Coordinate c2, Coordinate c3) {
        double m = countM(c1, c2);
        double b = c1.getY() - m * c1.getX();

        return c3.getY() > m * c3.getX() + b;
    }

    // Вычислить коэффициент наклона прямой y=m*x + b
    public static double countM(Coordinate c1, Coordinate c2) {
        return (c2.getY() - c1.getY()) / (c2.getX() - c1.getX());
    }

    public static boolean isAlmostParallelToYAxis(Coordinate c1, Coordinate c2) {
        double angle = Math.toDegrees(Math.atan2(c2.getY() - c1.getY(), c2.getX() - c1.getX()));
        double angleDifference = Math.abs(Math.abs(angle) - 90);
        return angleDifference <= ANGLE_ACCURACY;
    }

    public static boolean checkNotDataEmission(double newAngle, double previousAngle) {
        return previousAngle == 0.0 || Math.abs(newAngle - previousAngle) < DEGREE_ACCURACY;
    }

    public static Coordinate calculateAverage(Coordinate c1, Coordinate c2) {
        return Coordinate.builder()
                .x((c1.getX() + c2.getX()) / 2)
                .y((c1.getY() + c2.getY()) / 2)
                .z((c1.getZ() + c2.getZ()) / 2)
                .build();
    }

    // Расстояние между точками c1 и c2
    public static Double calculate2DDistance(Coordinate c1, Coordinate c2) {
        double dx = c1.getX() - c2.getX();
        double dy = c1.getY() - c2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Расстояние от точки c3 до прямой (c1;c2)
    public static double calculateDistanceFromPointToLine(Coordinate c1, Coordinate c2, Coordinate c3) {
        return Math.abs((c2.getY() - c1.getY()) * c3.getX() - (c2.getX() - c1.getX()) * c3.getY() + c2.getX() * c1.getY() - c2.getY() * c1.getX())
                / Math.sqrt(Math.pow(c2.getY() - c1.getY(), 2) + Math.pow(c2.getX() - c1.getX(), 2));
    }

    public static Coordinate getCoordinate(List<Coordinate> coordinates, List<BodyPart> bodyParts, BodyPart bodyPart) {
        return coordinates.get(bodyParts.indexOf(bodyPart));
    }
}
