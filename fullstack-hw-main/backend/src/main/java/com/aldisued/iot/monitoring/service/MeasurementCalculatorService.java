package com.aldisued.iot.monitoring.service;


import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MeasurementCalculatorService {

  public List<Double> filterByAverageDeviation(List<Double> values, Double deviation) {
    if (values == null || deviation == null || !Double.isFinite(deviation)
        || deviation < 0.0 || deviation > 1.0) {
      throw new IllegalArgumentException("Deviation must be a finite value between 0.0 and 1.0.");
    }
    if (values.isEmpty()) {
      return List.of();
    }
    if (values.stream().anyMatch(value -> value == null || !Double.isFinite(value))) {
      throw new IllegalArgumentException("Measurements must be finite numeric values.");
    }

    double average = values.stream().mapToDouble(Double::doubleValue).average()
        .orElseThrow();
    double allowedDeviation = Math.abs(average) * deviation;
    double lowerBound = Math.nextDown(average - allowedDeviation);
    double upperBound = Math.nextUp(average + allowedDeviation);
    return values.stream()
        .filter(value -> value >= lowerBound && value <= upperBound)
        .toList();
  }

  public List<Double> getMovingAverage(List<Double> data, int windowSize) {
    if (data == null || windowSize <= 0 || windowSize > data.size()) {
      throw new IllegalArgumentException(
          "Window size must be positive and no greater than the number of measurements.");
    }
    if (data.stream().anyMatch(value -> value == null || !Double.isFinite(value))) {
      throw new IllegalArgumentException("Measurements must be finite numeric values.");
    }

    var movingAverages = new ArrayList<Double>(data.size() - windowSize + 1);
    double windowTotal = 0.0;
    for (int i = 0; i < windowSize; i++) {
      windowTotal += data.get(i);
    }
    movingAverages.add(windowTotal / windowSize);

    for (int i = windowSize; i < data.size(); i++) {
      windowTotal += data.get(i) - data.get(i - windowSize);
      movingAverages.add(windowTotal / windowSize);
    }
    return List.copyOf(movingAverages);
  }

}
