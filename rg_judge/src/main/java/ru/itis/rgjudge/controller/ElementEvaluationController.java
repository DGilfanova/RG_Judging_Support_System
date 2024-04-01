package ru.itis.rgjudge.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.rgjudge.api.ElementEvaluationApi;
import ru.itis.rgjudge.dto.DefaultResponseDto;
import ru.itis.rgjudge.dto.ElementReport;
import ru.itis.rgjudge.dto.ElementResponse;
import ru.itis.rgjudge.service.ElementEvaluationService;
import ru.itis.rgjudge.service.ElementService;

import java.util.List;

@RestController
public class ElementEvaluationController implements ElementEvaluationApi {

    private final ElementService elementService;
    private final ElementEvaluationService elementEvaluationService;

    public ElementEvaluationController(ElementService elementService, ElementEvaluationService elementEvaluationService) {
        this.elementService = elementService;
        this.elementEvaluationService = elementEvaluationService;
    }

    @Override
    public DefaultResponseDto<List<ElementResponse>> getElements() {
        return DefaultResponseDto.<List<ElementResponse>>builder()
                .body(elementService.getAllElements())
                .build();
    }

    @Override
    public DefaultResponseDto<ElementReport> evaluateElement(Integer elementId, MultipartFile videoFile, MultipartFile gymnastPhoto) {
        return DefaultResponseDto.<ElementReport>builder()
                .body(elementEvaluationService.evaluateElement(elementId, videoFile, gymnastPhoto))
                .build();
    }
}
