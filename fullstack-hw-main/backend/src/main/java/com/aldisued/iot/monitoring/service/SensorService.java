package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SensorService {

  private final MonitoringStore monitoringStore;

  public SensorService(MonitoringStore monitoringStore) {
    this.monitoringStore = monitoringStore;
  }

  public Sensor saveSensor(SensorDto sensor) {
    if (sensor.name() == null || sensor.name().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sensor name is required.");
    }
    if (sensor.type() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sensor type is required.");
    }
    return monitoringStore.saveSensor(new Sensor(sensor.name().trim(), sensor.type()));
  }

  public List<Sensor> findAllSensors() {
    return monitoringStore.findAllSensors();
  }
}
