package com.dado.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

    private final int status;
    private final String message;

    private ErrorResponse(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(status, message);
    }
}
