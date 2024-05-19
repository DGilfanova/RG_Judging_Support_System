package ru.itis.rgjudge.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.rgjudge.db.enums.Side;
import ru.itis.rgjudge.dto.DefaultResponseDto;
import ru.itis.rgjudge.dto.ElementReport;
import ru.itis.rgjudge.dto.ElementResponse;

import java.io.IOException;
import java.util.List;

import static javax.swing.JSplitPane.RIGHT;

@RequestMapping("/api/v1/element")
public interface ElementEvaluationApi {

    @Operation(summary = "Получение списка элементов, по которым доступно оценивание")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    DefaultResponseDto<List<ElementResponse>> getElements();

    @Operation(summary = "Оценивание элемента")
    @PostMapping("/evaluate")
    @ResponseStatus(HttpStatus.OK)
    DefaultResponseDto<ElementReport> evaluateElement(
            @Parameter(description = "ID элемента") @RequestParam("elementId") Integer elementId,
            @Parameter(description = "Видео элемента (не более 10Мб)") @RequestParam("videoFile") MultipartFile videoFile,
            @Parameter(description = "ID элемента") @RequestParam(value = "handed", defaultValue = "RIGHT") Side handed
    ) throws IOException;
}
