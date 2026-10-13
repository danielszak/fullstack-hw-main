package com.aldisued.iot.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.aldisued.iot.monitoring.service.MeasurementCalculatorService;
import java.util.List;
import org.junit.jupiter.api.Test;

class MeasurementCalculatorServiceTests {

  private final MeasurementCalculatorService calculator = new MeasurementCalculatorService();

  @Test
  void filtersMeasurementsOutsideInclusiveAverageDeviationRange() {
    assertEquals(
        List.of(12.0),
        calculator.filterByAverageDeviation(List.of(10.0, 12.0, 20.0), 0.2)
    );
  }

  @Test
  void acceptsEmptyValuesAndRejectsInvalidDeviation() {
    assertEquals(List.of(), calculator.filterByAverageDeviation(List.of(), 0.2));
    assertThrows(IllegalArgumentException.class,
        () -> calculator.filterByAverageDeviation(List.of(1.0), -0.1));
    assertThrows(IllegalArgumentException.class,
        () -> calculator.filterByAverageDeviation(List.of(1.0), Double.NaN));
  }

  @Test
  void calculatesSlidingAveragesAndRejectsInvalidWindows() {
    assertEquals(
        List.of(20.0, 30.0, 40.0),
        calculator.getMovingAverage(List.of(10.0, 20.0, 30.0, 40.0, 50.0), 3)
    );
    assertEquals(List.of(10.0, 20.0), calculator.getMovingAverage(List.of(10.0, 20.0), 1));
    assertThrows(IllegalArgumentException.class,
        () -> calculator.getMovingAverage(List.of(10.0), 0));
    assertThrows(IllegalArgumentException.class,
        () -> calculator.getMovingAverage(List.of(10.0), 2));
  }
}
