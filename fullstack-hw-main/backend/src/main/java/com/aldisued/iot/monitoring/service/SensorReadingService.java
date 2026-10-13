package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SensorReadingService {

  private final SensorReadingRepository sensorReadingRepository;
  private final SensorRepository sensorRepository;

  public SensorReadingService(SensorReadingRepository sensorReadingRepository,
      SensorRepository sensorRepository) {
    this.sensorReadingRepository = sensorReadingRepository;
    this.sensorRepository = sensorRepository;
  }

  public SensorReading saveSensorReading(SensorReadingDto sensorReadingDto) {
    if (sensorReadingDto.sensorId() == null || sensorReadingDto.value() == null
        || sensorReadingDto.timestamp() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Sensor ID, reading value, and timestamp are required.");
    }
    var sensor = sensorRepository.findById(sensorReadingDto.sensorId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "The specified sensor was not found."));
    return sensorReadingRepository.save(new SensorReading(
        sensorReadingDto.value(),
        sensorReadingDto.timestamp(),
        sensor
    ));
  }

  public List<SensorReading> findAllSensorReadings() {
    return sensorReadingRepository.findAll();
  }

}
