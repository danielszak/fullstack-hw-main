package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.entity.Sensor;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {

  boolean existsByName(String name);

  List<Sensor> findAllByOrderByNameAsc();
}
