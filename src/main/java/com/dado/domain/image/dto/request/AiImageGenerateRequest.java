package com.dado.domain.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class AiImageGenerateRequest {

    @NotNull(message = "calendarId는 필수입니다.")
    private Long calendarId;

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate recordDate;

    @NotBlank(message = "키워드는 필수입니다.")
    private String aiKeyword;

    @NotBlank(message = "설명은 필수입니다.")
    private String aiDescription;
}
