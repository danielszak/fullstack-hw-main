package com.aldisued.iot.monitoring.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertResponseDto(
    Long id,
    UUID sensorId,
    String message,
    LocalDateTime timestamp
) {
}
