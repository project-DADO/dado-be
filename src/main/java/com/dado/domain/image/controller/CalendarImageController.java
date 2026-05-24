package com.dado.domain.image.controller;

import static org.springframework.http.HttpStatus.*;

import com.dado.domain.image.dto.request.AiImageConfirmRequest;
import com.dado.domain.image.dto.request.AiImageGenerateRequest;
import com.dado.domain.image.dto.request.DirectImageUploadRequest;
import com.dado.domain.image.dto.response.AiImageGenerateResponse;
import com.dado.domain.image.dto.response.CalendarImageResponse;
import com.dado.domain.image.service.CalendarImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendars/images")
@RequiredArgsConstructor
public class CalendarImageController {

    private final CalendarImageService calendarImageService;

    // 직접 그림 업로드
    @PostMapping("/direct")
    public ResponseEntity<CalendarImageResponse> uploadDirectImage(
            @Valid @ModelAttribute DirectImageUploadRequest request
    ) {
        CalendarImageResponse response = calendarImageService.uploadDirectImage(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    // AI 이미지 생성 요청
    @PostMapping("/ai/generate")
    public ResponseEntity<AiImageGenerateResponse> generateAiImage(
            @Valid @RequestBody AiImageGenerateRequest request
    ) {
        AiImageGenerateResponse response = calendarImageService.generateAiImage(request);
        return ResponseEntity.status(OK).body(response);
    }

    // AI 이미지 확정
    @PostMapping("/ai/confirm")
    public ResponseEntity<CalendarImageResponse> confirmAiImage(
            @Valid @RequestBody AiImageConfirmRequest request
    ) {
        CalendarImageResponse response = calendarImageService.confirmAiImage(request);
        return ResponseEntity.status(CREATED).body(response);
    }
}
