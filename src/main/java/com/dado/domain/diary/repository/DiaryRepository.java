package com.dado.domain.diary.repository;

import com.dado.domain.diary.entity.Diary;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    boolean existsByMemberIdAndRecordDate(Long memberId, LocalDate recordDate);
}
