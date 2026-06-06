package com.dado.domain.image.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DirectImageUploadRequest {

    @NotNull(message = "calendarId는 필수입니다.")
    private Long calendarId;

    @NotNull(message = "날짜는 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    @NotNull(message = "이미지 파일은 필수입니다.")
    private MultipartFile image;

    private boolean override;
}
