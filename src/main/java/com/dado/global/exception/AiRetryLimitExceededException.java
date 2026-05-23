package com.dado.global.exception;

public class AiRetryLimitExceededException extends RuntimeException {

    public AiRetryLimitExceededException(String message) {
        super(message);
    }
}
