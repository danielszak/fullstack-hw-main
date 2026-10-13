package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MonitoringStore {

  private final Map<UUID, Sensor> sensors = new LinkedHashMap<>();
  private final Map<Long, SensorReading> readings = new LinkedHashMap<>();
  private final Map<Long, Alert> alerts = new LinkedHashMap<>();
  private long nextReadingId = 1;
  private long nextAlertId = 1;

  public MonitoringStore() {
    var northGreenhouse = seedSensor(
        "10000000-0000-0000-0000-000000000001",
        "Greenhouse North",
        SensorType.TEMPERATURE
    );
    var southGreenhouse = seedSensor(
        "10000000-0000-0000-0000-000000000002",
        "Greenhouse South",
        SensorType.HUMIDITY
    );
    var coldStorage = seedSensor(
        "10000000-0000-0000-0000-000000000003",
        "Cold Storage",
        SensorType.TEMPERATURE
    );
    seedReading(northGreenhouse, 21.4, 8);
    seedReading(southGreenhouse, 48.2, 36);
    seedReading(coldStorage, 4.1, 62);
    seedAlert(northGreenhouse, "Temperature exceeded the configured upper limit.", 8);
    seedAlert(southGreenhouse, "Humidity is below the recommended range.", 42);
    seedAlert(coldStorage, "Sensor stopped reporting for 15 minutes.", 96);
  }

  private Sensor seedSensor(String id, String name,
      SensorType type) {
    var sensor = new Sensor(name, type);
    sensor.setId(UUID.fromString(id));
    sensors.put(sensor.getId(), sensor);
    return sensor;
  }

  private void seedReading(Sensor sensor, double value, long minutesAgo) {
    var reading = new SensorReading(
        value,
        LocalDateTime.now().minusMinutes(minutesAgo),
        sensor
    );
    saveReading(reading);
  }

  private void seedAlert(Sensor sensor, String message, long minutesAgo) {
    var alert = new Alert(
        message,
        LocalDateTime.now().minusMinutes(minutesAgo),
        sensor
    );
    saveAlert(alert);
  }

  public synchronized Sensor saveSensor(Sensor sensor) {
    boolean nameInUse = sensors.values().stream()
        .anyMatch(existing -> existing.getName().equals(sensor.getName()));
    if (nameInUse) {
      throw new SensorNameConflictException(sensor.getName());
    }
    if (sensor.getId() == null) {
      sensor.setId(UUID.randomUUID());
    }
    sensors.put(sensor.getId(), sensor);
    return sensor;
  }

  public synchronized List<Sensor> findAllSensors() {
    return sensors.values().stream()
        .sorted(Comparator.comparing(Sensor::getName))
        .toList();
  }

  public synchronized Optional<Sensor> findSensor(UUID sensorId) {
    return Optional.ofNullable(sensors.get(sensorId));
  }

  public synchronized SensorReading saveReading(SensorReading reading) {
    if (reading.getId() == null) {
      reading.setId(nextReadingId++);
    }
    readings.put(reading.getId(), reading);
    return reading;
  }

  public synchronized List<SensorReading> findAllReadings() {
    return new ArrayList<>(readings.values());
  }

  public synchronized List<Alert> findAllAlertsNewestFirst() {
    return alerts.values().stream()
        .sorted(Comparator.comparing(Alert::getTimestamp).reversed()
            .thenComparing(Alert::getId, Comparator.reverseOrder()))
        .toList();
  }

  public synchronized Alert saveAlert(Alert alert) {
    if (alert.getId() == null) {
      alert.setId(nextAlertId++);
    }
    alerts.put(alert.getId(), alert);
    return alert;
  }

  public synchronized Optional<Alert> findLatestAlert(UUID sensorId) {
    return alerts.values().stream()
        .filter(alert -> alert.getSensor().getId().equals(sensorId))
        .max(Comparator.comparing(Alert::getTimestamp)
            .thenComparing(Alert::getId));
  }

}
