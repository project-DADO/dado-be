package com.dado.domain.image.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class AiImageConfirmRequest {

    @NotNull(message = "calendarId는 필수입니다.")
    private Long calendarId;

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate recordDate;

    @NotNull(message = "logId는 필수입니다.")
    private Long logId;
}
