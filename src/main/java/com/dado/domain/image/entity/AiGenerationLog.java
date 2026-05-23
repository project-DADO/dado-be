package com.dado.domain.image.entity;

import com.dado.domain.calendar.entity.Calendar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Ai_Generation_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiGenerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "ai_keyword", nullable = false, length = 255)
    private String aiKeyword;

    @Column(name = "ai_description", nullable = false, columnDefinition = "TEXT")
    private String aiDescription;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "temp_image_url", nullable = false, length = 255)
    private String tempImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.retryCount == 0) {
            this.retryCount = 1;
        }
    }

    public static AiGenerationLog create(
            Calendar calendar,
            LocalDate recordDate,
            String aiKeyword,
            String aiDescription,
            int retryCount,
            String tempImageUrl
    ) {
        AiGenerationLog log = new AiGenerationLog();
        log.calendar = calendar;
        log.recordDate = recordDate;
        log.aiKeyword = aiKeyword;
        log.aiDescription = aiDescription;
        log.retryCount = retryCount;
        log.tempImageUrl = tempImageUrl;
        return log;
    }
}
