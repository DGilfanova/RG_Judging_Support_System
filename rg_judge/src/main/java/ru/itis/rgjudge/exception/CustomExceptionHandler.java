package ru.itis.rgjudge.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.itis.rgjudge.dto.DefaultResponseDto;

import java.nio.ByteBuffer;
import java.util.Optional;

@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public CustomExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<DefaultResponseDto<Object>> handleFeignException(FeignException feignException) {
        logger.error("handleFeignException: {}", feignException.getMessage(), feignException);

        Optional<ByteBuffer> exceptionBody = feignException.responseBody();
        DefaultResponseDto errorResponse = DefaultResponseDto.builder().build();
        if (exceptionBody.isPresent()) {
            try {
                errorResponse = objectMapper.readValue(exceptionBody.get().array(), DefaultResponseDto.class);
            } catch (Exception e) {
                errorResponse = DefaultResponseDto.builder()
                        .error(String.valueOf(feignException.status()))
                        .build();
            }
        }

        return ResponseEntity.status(feignException.status())
                .body(DefaultResponseDto.builder().error(errorResponse.error()).build());
    }

    @ExceptionHandler(ElementEvaluationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DefaultResponseDto<Object> handleElementEvaluationException(ElementEvaluationException exception) {
        logger.error("handleElementEvaluationException: {}", exception.getMessage(), exception);

        return DefaultResponseDto.builder().error(exception.getMessage()).build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public DefaultResponseDto<Object> handleAllException(RuntimeException exception) {
        logger.error("handleAllException: {}", exception.getMessage(), exception);

        return DefaultResponseDto.builder().error(exception.getMessage()).build();
    }
}
