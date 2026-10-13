package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MeasurementService {

  private final SensorReadingRepository sensorReadingRepository;

  public MeasurementService(SensorReadingRepository sensorReadingRepository) {
    this.sensorReadingRepository = sensorReadingRepository;
  }

  public List<Double> getMeasurementValuesBySensorType(SensorType sensorType, LocalDateTime from,
      LocalDateTime to) {
    if (sensorType == null || from == null || to == null || from.isAfter(to)) {
      throw new IllegalArgumentException("Sensor type and a valid time range are required.");
    }
    return sensorReadingRepository.findValues(sensorType, from, to);
  }

  public Optional<Double> getAverageTemperature(LocalDateTime from, LocalDateTime to) {
    if (from == null || to == null || from.isAfter(to)) {
      throw new IllegalArgumentException("A valid time range is required.");
    }
    return sensorReadingRepository.findAverageValue(SensorType.TEMPERATURE, from, to);
  }

}
