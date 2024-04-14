package ru.itis.rgjudge.service.impl;

import org.springframework.stereotype.Service;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.ElementReport;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.service.ReportService;

import java.util.ArrayList;
import java.util.List;

import static ru.itis.rgjudge.utils.Constant.IGNORE_ESTIMATION;
import static ru.itis.rgjudge.utils.Constant.ZERO_SCORE;

@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public ElementReport createReport(Element element, List<EstimatorResponse> estimatorResponses, String videoLink) {
        var isValid = true;
        var penalty = ZERO_SCORE;
        var detailedReport = new ArrayList<ReportData>();
        for (EstimatorResponse response: estimatorResponses) {
            var estimatorDetailedReport = response.reportData();
            if (!isValid) {
                estimatorDetailedReport.setPenalty(IGNORE_ESTIMATION);
                estimatorDetailedReport.setIsCounted(IGNORE_ESTIMATION);
            }
            switch (response.estimationType()) {
                case FULL_CHECK -> {
                    // Сбавка применяется только у критерия, из-за которого элемент не засчитан, более не сбавляем
                    if (isValid && !response.isValid()) {
                        penalty += response.penalty();
                    }
                    isValid = isValid && response.isValid();
                }
                case ONLY_PENALTY -> {
                    if (isValid && !response.isValid()) {
                        penalty += response.penalty();
                    }
                }
                case ONLY_VALIDNESS -> isValid = isValid && response.isValid();
            }
            detailedReport.add(estimatorDetailedReport);
        }
        var finalScore = isValid ? element.value() - penalty : ZERO_SCORE - penalty;
        System.out.println(ElementReport.builder()
            .elementName(element.name())
            .elementScore(element.value())
            .isValid(isValid)
            .penalty(penalty)
            .finalScore(finalScore)
            .detailedEstimatorReport(detailedReport)
            .videoLink(videoLink)
            .build());
        return ElementReport.builder()
                .elementName(element.name())
                .elementScore(element.value())
                .isValid(isValid)
                .penalty(penalty)
                .finalScore(finalScore)
                .detailedEstimatorReport(detailedReport)
                .videoLink(videoLink)
                .build();
    }
}
