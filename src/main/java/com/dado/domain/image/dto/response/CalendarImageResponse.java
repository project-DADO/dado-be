package com.dado.domain.image.dto.response;

import com.dado.domain.image.entity.CalendarImage;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CalendarImageResponse {

    private final Long imageId;
    private final String imageUrl;
    private final LocalDate recordDate;
    private final String originType;

    public CalendarImageResponse(Long imageId, String imageUrl,
                                 LocalDate recordDate, String originType) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.recordDate = recordDate;
        this.originType = originType;
    }

    public static CalendarImageResponse from(CalendarImage image) {
        return new CalendarImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getRecordDate(),
                image.getOriginType().name()
        );
    }
}
