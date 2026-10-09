package com.aldisued.iot.monitoring.tasks;

import com.aldisued.iot.monitoring.service.MeasurementCalculatorService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class Task10Tests {

  private final MeasurementCalculatorService measurementCalculatorService
      = new MeasurementCalculatorService();

  @ParameterizedTest
  @MethodSource("movingAverageSource")
  public void verifyGetMovingAverage(
      List<Double> values,
      int windowSize,
      List<Double> expectedValues) {
    List<Double> movingAverage = measurementCalculatorService.getMovingAverage(
        values, windowSize);

    Assertions.assertEquals(expectedValues, movingAverage);
  }

  @ParameterizedTest
  @MethodSource("invalidInputSource")
  public void verifyInvalidInputHandling(
      List<Double> values,
      int windowSize) {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> measurementCalculatorService.getMovingAverage(values, windowSize));
  }


  static Stream<Arguments> movingAverageSource() {
    return Stream.of(
        Arguments.of(
            List.of(10.0, 11.0, 12.0),
            1,
            List.of(10.0, 11.0, 12.0)
        ),
        Arguments.of(
            List.of(8.0, 13.0, 15.0, 20.0, 25.0),
            3,
            List.of(12.0, 16.0, 20.0)
        )
    );
  }

  static Stream<Arguments> invalidInputSource() {
    return Stream.of(
        Arguments.of(
            List.of(10.0, 11.0, 12.0),
            0
        ),
        Arguments.of(
            List.of(10.0, 11.0, 12.0),
            -1
        ),
        Arguments.of(
            Collections.emptyList(),
            1
        )
    );
  }

}
