package com.dado.domain.image.dto.response;

import com.dado.domain.image.entity.AiGenerationLog;
import lombok.Getter;

@Getter
public class AiImageGenerateResponse {

    private final Long logId;
    private final String tempUrl;
    private final int retryCount;
    private final String originType;

    public AiImageGenerateResponse(Long logId, String tempUrl, int retryCount) {
        this.logId = logId;
        this.tempUrl = tempUrl;
        this.retryCount = retryCount;
        this.originType = "AI";
    }

    public static AiImageGenerateResponse from(AiGenerationLog log) {
        return new AiImageGenerateResponse(
                log.getId(),
                log.getTempImageUrl(),
                log.getRetryCount()
        );
    }
}
