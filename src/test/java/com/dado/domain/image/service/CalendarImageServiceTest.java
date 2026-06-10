package com.dado.domain.image.service;

import static com.dado.domain.image.entity.CalendarImage.OriginType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.dado.domain.calendar.entity.Calendar;
import com.dado.domain.calendar.repository.CalendarRepository;
import com.dado.domain.image.dto.request.DirectImageUploadRequest;
import com.dado.domain.image.dto.response.CalendarImageResponse;
import com.dado.domain.image.entity.CalendarImage;
import com.dado.domain.image.repository.AiGenerationLogRepository;
import com.dado.domain.image.repository.CalendarImageRepository;
import com.dado.global.exception.DuplicateImageException;
import com.dado.global.exception.InvalidFileException;
import com.dado.global.exception.NotFoundException;
import com.dado.global.storage.StorageService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class CalendarImageServiceTest {

    @InjectMocks
    private CalendarImageService calendarImageService;

    @Mock
    private Calendar calendar;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private CalendarImageRepository calendarImageRepository;

    @Mock
    private AiGenerationLogRepository aiGenerationLogRepository;

    @Mock
    private StorageService storageService;

    private CalendarImage existingImage;
    private MockMultipartFile mockFile;
    private final Long CALENDAR_ID = 1L;
    private final LocalDate RECORD_DATE = LocalDate.of(2026, 6, 6);

    @BeforeEach
    void setUp() {
        // 공통으로 쓸 테스트 객체 세팅
        existingImage = CalendarImage.create(
                calendar,
                RECORD_DATE,
                "/uploads/old-image.png",
                DIRECT
        );
        mockFile = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                "fake-image-content".getBytes()
        );
    }

    @Test
    @DisplayName("정상 업로드 - 새 이미지가 저장되고 URL이 반환된다")
    void uploadDirectImage_success() {
        // given
        DirectImageUploadRequest request = createRequest(false, mockFile);

        given(calendarRepository.findById(CALENDAR_ID))
                .willReturn(Optional.of(calendar));
        given(calendarImageRepository.findByCalendarIdAndRecordDate(CALENDAR_ID, RECORD_DATE))
                .willReturn(Optional.empty()); // 기존 이미지 없음
        given(storageService.upload(mockFile))
                .willReturn("/uploads/new-image.png");
        given(calendarImageRepository.save(any(CalendarImage.class)))
                .willReturn(existingImage);

        // when
        CalendarImageResponse response = calendarImageService.uploadDirectImage(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOriginType()).isEqualTo("DIRECT");
        then(storageService).should(times(1)).upload(mockFile);
        then(calendarImageRepository).should(times(1)).save(any(CalendarImage.class));
    }

    @Test
    @DisplayName("override=false + 중복 날짜 - DuplicateImageException 발생")
    void uploadDirectImage_중복날짜_override_false() {
        // given
        DirectImageUploadRequest request = createRequest(false, mockFile);

        given(calendarRepository.findById(CALENDAR_ID))
                .willReturn(Optional.of(calendar));
        given(calendarImageRepository.findByCalendarIdAndRecordDate(CALENDAR_ID, RECORD_DATE))
                .willReturn(Optional.of(existingImage)); // 기존 이미지 있음

        // when & then
        assertThatThrownBy(() -> calendarImageService.uploadDirectImage(request))
                .isInstanceOf(DuplicateImageException.class)
                .hasMessage("이미 해당 날짜에 이미지가 존재합니다. 덮어쓰시겠습니까?");

        then(storageService).should(never()).upload(any()); // 업로드 호출 안 됨
    }

    @Test
    @DisplayName("override=true + 중복 날짜 - 기존 파일 삭제 후 새 이미지로 교체됨")
    void uploadDirectImage_중복날짜_override_true() {
        // given
        DirectImageUploadRequest request = createRequest(true, mockFile);

        given(calendarRepository.findById(CALENDAR_ID))
                .willReturn(Optional.of(calendar));
        given(calendarImageRepository.findByCalendarIdAndRecordDate(CALENDAR_ID, RECORD_DATE))
                .willReturn(Optional.of(existingImage)); // 기존 이미지 있음
        given(storageService.upload(mockFile))
                .willReturn("/uploads/new-image.png");

        // when
        CalendarImageResponse response = calendarImageService.uploadDirectImage(request);

        // then
        assertThat(response.getImageUrl()).isEqualTo("/uploads/new-image.png");
        then(storageService).should(times(1)).delete("/uploads/old-image.png");
        then(storageService).should(times(1)).upload(mockFile);
        then(calendarImageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 calendarId - NotFoundException이 발생한다")
    void uploadDirectImage_존재하지않는_calendarId() {
        // given
        DirectImageUploadRequest request = createRequest(false, mockFile);

        given(calendarRepository.findById(CALENDAR_ID))
                .willReturn(Optional.empty()); // calendar 없음

        // when & then
        assertThatThrownBy(() -> calendarImageService.uploadDirectImage(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("캘린더를 찾을 수 없습니다.");

        then(calendarImageRepository).should(never()).findByCalendarIdAndRecordDate(any(), any());
        then(storageService).should(never()).upload(any());
    }

    @Test
    @DisplayName("파일 없이 요청 - InvalidFileException이 발생한다")
    void uploadDirectImage_파일없음() {
        // given
        DirectImageUploadRequest request = createRequest(false, null); // 파일 없음

        given(calendarRepository.findById(CALENDAR_ID))
                .willReturn(Optional.of(calendar));
        given(calendarImageRepository.findByCalendarIdAndRecordDate(CALENDAR_ID, RECORD_DATE))
                .willReturn(Optional.empty());
        given(storageService.upload(null))
                .willThrow(new InvalidFileException("파일이 비어있습니다."));

        // when & then
        assertThatThrownBy(() -> calendarImageService.uploadDirectImage(request))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("파일이 비어있습니다.");
    }

    private DirectImageUploadRequest createRequest(boolean override, MockMultipartFile file) {
        DirectImageUploadRequest request = new DirectImageUploadRequest();
        request.setCalendarId(CALENDAR_ID);
        request.setRecordDate(RECORD_DATE);
        request.setOverride(override);
        request.setImage(file);
        return request;
    }
}
