package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SensorService {

  private final SensorRepository sensorRepository;

  public SensorService(SensorRepository sensorRepository) {
    this.sensorRepository = sensorRepository;
  }

  public Sensor saveSensor(SensorDto sensor) {
    if (sensor.name() == null || sensor.name().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sensor name is required.");
    }
    if (sensor.type() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sensor type is required.");
    }
    var name = sensor.name().trim();
    if (sensorRepository.existsByName(name)) {
      throw new SensorNameConflictException(name);
    }
    try {
      return sensorRepository.save(new Sensor(name, sensor.type()));
    } catch (DataIntegrityViolationException e) {
      // Concurrent insert won the race on the unique name constraint.
      throw new SensorNameConflictException(name);
    }
  }

  public List<Sensor> findAllSensors() {
    return sensorRepository.findAllByOrderByNameAsc();
  }
}
