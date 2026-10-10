package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.service.AlertService;
import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.dto.AlertResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("/latest")
  public AlertDto getLatestAlert(@RequestParam UUID sensorId) {
    return alertService.findLastAlertBySensorId(sensorId);
  }
}
