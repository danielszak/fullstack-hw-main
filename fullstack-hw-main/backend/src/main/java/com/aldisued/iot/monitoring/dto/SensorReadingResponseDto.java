package com.aldisued.iot.monitoring.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SensorReadingResponseDto(
    Long id,
    UUID sensorId,
    Double value,
    LocalDateTime timestamp
) {
}
