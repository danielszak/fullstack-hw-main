package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.dto.AlertResponseDto;
import com.aldisued.iot.monitoring.entity.Alert;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlertService {

  private final MonitoringStore monitoringStore;
  private final ObjectProvider<AlertKafkaPublisher> alertKafkaPublisher;

  public AlertService(
      MonitoringStore monitoringStore,
      ObjectProvider<AlertKafkaPublisher> alertKafkaPublisher) {
    this.monitoringStore = monitoringStore;
    this.alertKafkaPublisher = alertKafkaPublisher;
  }

  public Alert saveAlert(AlertDto alertDto) {
    if (alertDto.sensorId() == null || alertDto.message() == null
        || alertDto.timestamp() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Sensor ID, alert message, and timestamp are required.");
    }
    var sensor = monitoringStore.findSensor(alertDto.sensorId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "The specified sensor was not found."));
    var alert = monitoringStore.saveAlert(new Alert(
        alertDto.message(),
        alertDto.timestamp(),
        sensor
    ));
    var publisher = alertKafkaPublisher.getIfAvailable();
    if (publisher != null) {
      publisher.publish(alertDto);
    }
    return alert;
  }

  public AlertDto findLastAlertBySensorId(UUID sensorId) {
    return monitoringStore.findLatestAlert(sensorId)
        .map(alert -> new AlertDto(
            alert.getSensor().getId(),
            alert.getMessage(),
            alert.getTimestamp()
        ))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No alert was found for the specified sensor."));
  }

  public List<AlertResponseDto> findAllAlerts() {
    return monitoringStore.findAllAlertsNewestFirst().stream()
        .map(alert -> new AlertResponseDto(
            alert.getId(),
            alert.getSensor().getId(),
            alert.getMessage(),
            alert.getTimestamp()
        ))
        .toList();
  }
}
