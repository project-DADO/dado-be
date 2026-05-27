package com.dado.domain.image.service;

import com.dado.domain.calendar.entity.Calendar;
import com.dado.domain.calendar.repository.CalendarRepository;
import com.dado.domain.image.dto.request.AiImageConfirmRequest;
import com.dado.domain.image.dto.request.AiImageGenerateRequest;
import com.dado.domain.image.dto.request.DirectImageUploadRequest;
import com.dado.domain.image.dto.response.AiImageGenerateResponse;
import com.dado.domain.image.dto.response.CalendarImageResponse;
import com.dado.domain.image.entity.AiGenerationLog;
import com.dado.domain.image.entity.CalendarImage;
import com.dado.domain.image.repository.AiGenerationLogRepository;
import com.dado.domain.image.repository.CalendarImageRepository;
import com.dado.global.exception.AiRetryLimitExceededException;
import com.dado.global.exception.DuplicateImageException;
import com.dado.global.exception.NotFoundException;
import com.dado.global.storage.StorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarImageService {

    private static final int AI_RETRY_LIMIT = 5;

    private final CalendarRepository calendarRepository;
    private final CalendarImageRepository calendarImageRepository;
    private final AiGenerationLogRepository aiGenerationLogRepository;
    private final StorageService storageService;

    // 직접 그림 업로드
    @Transactional
    public CalendarImageResponse uploadDirectImage(DirectImageUploadRequest request) {

        Calendar calendar = findCalendar(request.getCalendarId());

        // 중복 날짜 체크
        CalendarImage existingImage = calendarImageRepository
                .findByCalendarIdAndRecordDate(request.getCalendarId(), request.getRecordDate())
                .orElse(null);

        if (existingImage != null && !request.isOverride()) {
            throw new DuplicateImageException("이미 해당 날짜에 이미지가 존재합니다. 덮어쓰시겠습니까?");
        }

        // 새 파일 먼저 업로드 (실패해도 기존 파일 안전)
        String imageUrl = storageService.upload(request.getImage());

        // 업로드 성공 후 기존 파일 삭제 및 DB 처리
        if (existingImage != null) {
            storageService.delete(existingImage.getImageUrl());
            existingImage.updateImageUrl(imageUrl);
            return CalendarImageResponse.from(existingImage);
        }

        CalendarImage savedImage = calendarImageRepository.save(
                CalendarImage.create(
                        calendar,
                        request.getRecordDate(),
                        imageUrl,
                        CalendarImage.OriginType.DIRECT
                )
        );

        return CalendarImageResponse.from(savedImage);
    }

    // AI 이미지 생성 요청
    @Transactional
    public AiImageGenerateResponse generateAiImage(AiImageGenerateRequest request) {

        Calendar calendar = findCalendar(request.getCalendarId());

        // 재시도 횟수 체크
        int retryCount = aiGenerationLogRepository
                .countByCalendarIdAndRecordDate(request.getCalendarId(), request.getRecordDate());

        if (retryCount >= AI_RETRY_LIMIT) {
            throw new AiRetryLimitExceededException("생성 가능한 횟수(5회)를 초과했습니다.");
        }

        // AI API 호출 (현재는 Mock)
        String tempImageUrl = callAiApi(request.getAiKeyword(), request.getAiDescription());

        // AiGenerationLog 저장
        AiGenerationLog log = aiGenerationLogRepository.save(
                AiGenerationLog.create(
                        calendar,
                        request.getRecordDate(),
                        request.getAiKeyword(),
                        request.getAiDescription(),
                        retryCount + 1,
                        tempImageUrl
                )
        );

        return AiImageGenerateResponse.from(log);
    }

    // AI 이미지 확정
    @Transactional
    public CalendarImageResponse confirmAiImage(AiImageConfirmRequest request) {

        Calendar calendar = findCalendar(request.getCalendarId());

        // logId + calendarId + recordDate 세 가지 동시 검증
        AiGenerationLog selectedLog = aiGenerationLogRepository
                .findByIdAndCalendarIdAndRecordDate(
                        request.getLogId(),
                        request.getCalendarId(),
                        request.getRecordDate()
                )
                .orElseThrow(() -> new NotFoundException("해당 AI 생성 기록을 찾을 수 없습니다."));

        // 중복 날짜 체크
        if (calendarImageRepository.existsByCalendarIdAndRecordDate(
                request.getCalendarId(), request.getRecordDate())) {
            throw new DuplicateImageException("이미 해당 날짜에 확정된 이미지가 존재합니다.");
        }

        // 나머지 임시 파일 삭제
        List<AiGenerationLog> allLogs = aiGenerationLogRepository
                .findByCalendarIdAndRecordDate(request.getCalendarId(), request.getRecordDate());

        allLogs.stream()
                .filter(log -> !log.getId().equals(request.getLogId()))
                .forEach(log -> storageService.delete(log.getTempImageUrl()));

        aiGenerationLogRepository.deleteAll(allLogs);

        // CalendarImage 저장
        CalendarImage savedImage = calendarImageRepository.save(
                CalendarImage.create(
                        calendar,
                        request.getRecordDate(),
                        selectedLog.getTempImageUrl(),
                        CalendarImage.OriginType.AI
                )
        );

        return CalendarImageResponse.from(savedImage);
    }

    private Calendar findCalendar(Long calendarId) {
        return calendarRepository.findById(calendarId)
                .orElseThrow(() -> new NotFoundException("캘린더를 찾을 수 없습니다."));
    }

    private String callAiApi(String keyword, String description) {
        // TODO: 실제 AI API 연동 시 교체
        log.info("AI API 호출 - keyword: {}, description: {}", keyword, description);
        return storageService.upload(null); // 임시 Mock
    }
}
