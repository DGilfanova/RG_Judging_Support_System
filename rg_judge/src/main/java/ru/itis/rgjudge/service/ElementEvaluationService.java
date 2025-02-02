package ru.itis.rgjudge.service;

import org.springframework.web.multipart.MultipartFile;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.dto.ElementReport;

public interface ElementEvaluationService {
    ElementReport evaluateElement(Integer elementId, MultipartFile videoFile, Side handed);
}
