package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.entity.Alert;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

  Optional<Alert> findFirstBySensorIdOrderByTimestampDescIdDesc(UUID sensorId);

  List<Alert> findAllByOrderByTimestampDescIdDesc();
}
