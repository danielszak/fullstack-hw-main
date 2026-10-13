package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.SensorReading;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SensorReadingService {

  private final MonitoringStore monitoringStore;

  public SensorReadingService(MonitoringStore monitoringStore) {
    this.monitoringStore = monitoringStore;
  }

  public SensorReading saveSensorReading(SensorReadingDto sensorReadingDto) {
    if (sensorReadingDto.sensorId() == null || sensorReadingDto.value() == null
        || sensorReadingDto.timestamp() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Sensor ID, reading value, and timestamp are required.");
    }
    var sensor = monitoringStore.findSensor(sensorReadingDto.sensorId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "The specified sensor was not found."));
    var reading = new SensorReading(
        sensorReadingDto.value(),
        sensorReadingDto.timestamp(),
        sensor
    );
    return monitoringStore.saveReading(reading);
  }

  public List<SensorReading> findAllSensorReadings() {
    return monitoringStore.findAllReadings();
  }

}
