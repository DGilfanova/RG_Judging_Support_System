package ru.itis.rgjudge.service;

import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.dto.ElementReport;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;

import java.util.List;

public interface ReportService {

    ElementReport createReport(Element element, List<EstimatorResponse> estimatorResponses, String videoLink);
}
