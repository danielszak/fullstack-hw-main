package com.aldisued.iot.monitoring.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlertDto(
    @NotNull
    UUID sensorId,
    @NotBlank
    String message,
    @NotNull
    LocalDateTime timestamp
) {
}
