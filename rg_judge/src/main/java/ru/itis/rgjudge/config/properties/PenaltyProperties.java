package ru.itis.rgjudge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
    Сбавки вынесены в свойства, т.к. имеют тенденцию часто меняться
    degreeTable - сбавки за стандартные отклонения (корпуса, шпагата и т.д): <10, <20, >20
    bentLeg - сбавка за согнутую часть тела
 */
@ConfigurationProperties("penalty")
public record PenaltyProperties(
        StandardDeviation standardDeviation,
        Double bentBodyPart,
        Double notReleve,
        Double fallFromReleve
) {

    public record StandardDeviation(
            Double smallDeviation,
            Double largeDeviation,
            Double lessThanSmallDeviation,
            Double lessThanLargeDeviation,
            Double moreThanLargeDeviation
    ) {
    }
}
