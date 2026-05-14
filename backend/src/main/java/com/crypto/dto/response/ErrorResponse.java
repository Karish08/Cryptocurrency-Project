package com.crypto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private java.util.Map<String, String> details;
    private String path;

    public ErrorResponse(String message) {
        this.message = message;
        this.status = 400;
        this.timestamp = LocalDateTime.now();
    }
}
