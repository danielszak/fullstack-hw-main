package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.service.AlertService;
import com.aldisued.iot.monitoring.dto.AlertResponseDto;
import com.aldisued.iot.monitoring.dto.AlertDto;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alerts")
public class AlertController {

  private final AlertService alertService;

  public AlertController(AlertService alertService) {
    this.alertService = alertService;
  }

  @GetMapping
  public List<AlertResponseDto> getAlerts() {
    return alertService.findAllAlerts();
  }

  @PostMapping
  public AlertResponseDto createAlert(@Valid @RequestBody AlertDto alertDto) {
    var alert = alertService.saveAlert(alertDto);
    return new AlertResponseDto(
        alert.getId(),
        alert.getSensor().getId(),
        alert.getMessage(),
        alert.getTimestamp()
    );
  }

  @GetMapping("/latest")
  public AlertDto getLatestAlert(@RequestParam UUID sensorId) {
    return alertService.findLastAlertBySensorId(sensorId);
  }
}
