package com.dado.domain.image.repository;

import com.dado.domain.image.entity.AiGenerationLog;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiGenerationLogRepository extends JpaRepository<AiGenerationLog, Long> {

    int countByCalendarIdAndRecordDate(Long calendarId, LocalDate recordDate);
}
