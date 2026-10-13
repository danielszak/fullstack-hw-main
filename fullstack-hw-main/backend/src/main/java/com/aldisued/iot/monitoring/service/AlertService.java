package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.dto.AlertResponseDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlertService {

  private static final String ALERTS_TOPIC = "alerts";

  private final AlertRepository alertRepository;
  private final SensorRepository sensorRepository;
  private final KafkaTemplate<String, AlertDto> kafkaTemplate;

  public AlertService(AlertRepository alertRepository, SensorRepository sensorRepository,
      KafkaTemplate<String, AlertDto> kafkaTemplate) {
    this.alertRepository = alertRepository;
    this.sensorRepository = sensorRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  public Alert saveAlert(AlertDto alertDto) {
    if (alertDto.sensorId() == null || alertDto.message() == null
        || alertDto.timestamp() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Sensor ID, alert message, and timestamp are required.");
    }
    var sensor = sensorRepository.findById(alertDto.sensorId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "The specified sensor was not found."));
    var alert = alertRepository.save(new Alert(
        alertDto.message(),
        alertDto.timestamp(),
        sensor
    ));
    kafkaTemplate.send(ALERTS_TOPIC, alertDto);
    return alert;
  }

  public AlertDto findLastAlertBySensorId(UUID sensorId) {
    return alertRepository.findFirstBySensorIdOrderByTimestampDescIdDesc(sensorId)
        .map(alert -> new AlertDto(
            alert.getSensor().getId(),
            alert.getMessage(),
            alert.getTimestamp()
        ))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No alert was found for the specified sensor."));
  }

  public List<AlertResponseDto> findAllAlerts() {
    return alertRepository.findAllByOrderByTimestampDescIdDesc().stream()
        .map(alert -> new AlertResponseDto(
            alert.getId(),
            alert.getSensor().getId(),
            alert.getMessage(),
            alert.getTimestamp()
        ))
        .toList();
  }
}
