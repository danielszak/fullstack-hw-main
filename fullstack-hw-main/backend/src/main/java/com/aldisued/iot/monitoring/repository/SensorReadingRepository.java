package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

  @Query("""
      select avg(r.value) from SensorReading r
      where r.sensor.type = :type and r.timestamp between :from and :to
      """)
  Optional<Double> findAverageValue(@Param("type") SensorType type,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("""
      select r.value from SensorReading r
      where r.sensor.type = :type and r.timestamp between :from and :to
      order by r.timestamp, r.id
      """)
  List<Double> findValues(@Param("type") SensorType type,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
