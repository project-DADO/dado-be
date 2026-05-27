package com.dado.global.response;

import static org.springframework.http.HttpStatus.*;

import com.dado.global.exception.AiRetryLimitExceededException;
import com.dado.global.exception.DuplicateImageException;
import com.dado.global.exception.FileStorageException;
import com.dado.global.exception.InvalidFileException;
import com.dado.global.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 파일 (크기, 빈 파일 등) → 400
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileException(InvalidFileException e) {
        log.warn("잘못된 파일 요청: {}", e.getMessage());
        return toErrorResponse(BAD_REQUEST, e.getMessage());
    }

    // 잘못된 인자 → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 인자: {}", e.getMessage());
        return toErrorResponse(BAD_REQUEST, e.getMessage());
    }

    // @Valid 검증 실패 → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        log.warn("유효성 검증 실패: {}", message);
        return toErrorResponse(BAD_REQUEST, message);
    }

    // 존재하지 않는 리소스 → 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        log.warn("리소스 없음: {}", e.getMessage());
        return toErrorResponse(NOT_FOUND, e.getMessage());
    }

    // 중복 날짜 이미지 삽입 → 409
    @ExceptionHandler(DuplicateImageException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateImageException(DuplicateImageException e) {
        log.warn("중복 이미지 요청: {}", e.getMessage());
        return toErrorResponse(CONFLICT, e.getMessage());
    }

    // AI 생성 횟수 초과 → 403
    @ExceptionHandler(AiRetryLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleAiRetryLimitExceededException(AiRetryLimitExceededException e) {
        log.warn("AI 생성 횟수 초과: {}", e.getMessage());
        return toErrorResponse(FORBIDDEN, e.getMessage());
    }

    // 파일 저장,삭제 실패 (서버 인프라 문제) → 500
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException e) {
        log.error("파일 저장/삭제 실패: {}", e.getMessage(), e);
        return toErrorResponse(INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.");
    }

    // 그 외 예상치 못한 예외 → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
        return toErrorResponse(INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> toErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status, message));
    }
}
