package com.dado.domain.image.repository;

import com.dado.domain.image.entity.CalendarImage;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarImageRepository extends JpaRepository<CalendarImage, Long> {

    boolean existsByCalendarIdAndRecordDate(Long calendarId, LocalDate recordDate);

    Optional<CalendarImage> findByCalendarIdAndRecordDate(Long calendarId, LocalDate recordDate);
}
