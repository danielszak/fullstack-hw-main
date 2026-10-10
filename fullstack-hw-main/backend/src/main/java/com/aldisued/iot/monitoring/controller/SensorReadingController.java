package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.dto.SensorReadingResponseDto;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.service.SensorReadingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sensor-readings")
public class SensorReadingController {

  private final SensorReadingService sensorReadingService;

  public SensorReadingController(SensorReadingService sensorReadingService) {
    this.sensorReadingService = sensorReadingService;
  }

  @PostMapping
  public SensorReadingResponseDto saveSensorReading(
      @Valid @RequestBody SensorReadingDto sensorReadingDto) {
    return toResponse(sensorReadingService.saveSensorReading(sensorReadingDto));
  }

  @GetMapping
  public List<SensorReadingResponseDto> getSensorReadings() {
    return sensorReadingService.findAllSensorReadings().stream()
        .map(SensorReadingController::toResponse)
        .toList();
  }

  private static SensorReadingResponseDto toResponse(SensorReading reading) {
    return new SensorReadingResponseDto(
        reading.getId(),
        reading.getSensor().getId(),
        reading.getValue(),
        reading.getTimestamp()
    );
  }
}
