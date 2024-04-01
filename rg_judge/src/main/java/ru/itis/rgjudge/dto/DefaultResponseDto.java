package ru.itis.rgjudge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DefaultResponseDto<T>(String error, T body) {
}
