package ru.itis.rgjudge.service;

public interface PenaltyScorer {
    Double getPenaltyByAngle(Double deflectionAngle);
    Double getPenaltyForBentBodyPart();
    Double getPenaltyForNotReleve();
    Double getPenaltyForFallingFromReleve();
}
