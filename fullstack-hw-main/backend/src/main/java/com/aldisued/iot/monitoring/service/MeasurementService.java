package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MeasurementService {

  private final MonitoringStore monitoringStore;

  public MeasurementService(MonitoringStore monitoringStore) {
    this.monitoringStore = monitoringStore;
  }

  public List<Double> getMeasurementValuesBySensorType(SensorType sensorType, LocalDateTime from,
      LocalDateTime to) {
    if (sensorType == null || from == null || to == null || from.isAfter(to)) {
      throw new IllegalArgumentException("Sensor type and a valid time range are required.");
    }
    return monitoringStore.findAllReadings().stream()
        .filter(reading -> reading.getSensor().getType() == sensorType)
        .filter(reading -> !reading.getTimestamp().isBefore(from)
            && !reading.getTimestamp().isAfter(to))
        .sorted(Comparator.comparing(SensorReading::getTimestamp))
        .map(SensorReading::getValue)
        .toList();
  }

  public Optional<Double> getAverageTemperature(LocalDateTime from, LocalDateTime to) {
    var average = monitoringStore.findAllReadings().stream()
        .filter(reading -> reading.getSensor().getType() == SensorType.TEMPERATURE)
        .filter(reading -> !reading.getTimestamp().isBefore(from)
            && !reading.getTimestamp().isAfter(to))
        .mapToDouble(SensorReading::getValue)
        .average();
    return average.isPresent() ? Optional.of(average.getAsDouble()) : Optional.empty();
  }

}
