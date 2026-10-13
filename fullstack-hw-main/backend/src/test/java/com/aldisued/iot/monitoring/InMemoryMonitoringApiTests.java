package com.aldisued.iot.monitoring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.service.MeasurementService;
import com.aldisued.iot.monitoring.service.MonitoringStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class InMemoryMonitoringApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MonitoringStore monitoringStore;

  @Autowired
  private MeasurementService measurementService;

  @Test
  void rejectsSensorsWithoutNamesAndDuplicateNames() throws Exception {
    mockMvc.perform(post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new SensorDto(null, SensorType.TEMPERATURE))))
        .andExpect(status().isBadRequest());

    var sensor = new SensorDto("duplicate-" + UUID.randomUUID(), SensorType.TEMPERATURE);
    mockMvc.perform(post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sensor)))
        .andExpect(status().isOk());
    mockMvc.perform(post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sensor)))
        .andExpect(status().isConflict());
  }

  @Test
  void createsAndListsSensorsAndReadings() throws Exception {
    var sensor = new SensorDto("api-" + UUID.randomUUID(), SensorType.TEMPERATURE);
    var sensorResponse = mockMvc.perform(post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sensor)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("TEMPERATURE"))
        .andReturn();
    var sensorId = objectMapper.readTree(sensorResponse.getResponse().getContentAsString())
        .get("id").asText();

    mockMvc.perform(get("/sensors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '%s')].name".formatted(sensorId)).exists());

    var reading = new SensorReadingDto(
        UUID.fromString(sensorId),
        22.5,
        LocalDateTime.parse("2026-10-09T10:00:00")
    );
    mockMvc.perform(post("/sensor-readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reading)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value(22.5));

    mockMvc.perform(get("/sensor-readings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.sensorId == '%s')].value".formatted(sensorId)).exists());
  }

  @Test
  void calculatesAverageForTemperatureReadingsWithinInclusiveBounds() {
    var temperatureSensor = monitoringStore.saveSensor(
        new Sensor("average-temp-" + UUID.randomUUID(), SensorType.TEMPERATURE));
    var humiditySensor = monitoringStore.saveSensor(
        new Sensor("average-humidity-" + UUID.randomUUID(), SensorType.HUMIDITY));
    var from = LocalDateTime.parse("2020-10-09T10:00:00");
    var to = LocalDateTime.parse("2020-10-09T11:00:00");
    monitoringStore.saveReading(new SensorReading(10.0, from, temperatureSensor));
    monitoringStore.saveReading(new SensorReading(20.0, to, temperatureSensor));
    monitoringStore.saveReading(new SensorReading(100.0, from, humiditySensor));

    org.junit.jupiter.api.Assertions.assertEquals(
        java.util.Optional.of(15.0),
        measurementService.getAverageTemperature(from, to)
    );
    org.junit.jupiter.api.Assertions.assertTrue(
        measurementService.getAverageTemperature(to.plusSeconds(1), to.plusHours(1)).isEmpty()
    );
  }

  @Test
  void returnsNotFoundForUnknownLatestAlertAndBadRequestWithoutSensorId() throws Exception {
    mockMvc.perform(get("/alerts/latest"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(get("/alerts/latest").queryParam("sensorId", UUID.randomUUID().toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  void returnsTheMostRecentAlertForItsSensor() throws Exception {
    var latestMessage = monitoringStore.findLatestAlert(
            UUID.fromString("10000000-0000-0000-0000-000000000001"))
        .orElseThrow()
        .getMessage();
    mockMvc.perform(get("/alerts/latest")
            .queryParam("sensorId", "10000000-0000-0000-0000-000000000001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(latestMessage));

    var firstMessage = monitoringStore.findAllAlertsNewestFirst().get(0).getMessage();
    mockMvc.perform(get("/alerts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].message").value(firstMessage));
  }

  @Test
  void createsAnAlertInMemoryAndMakesItAvailableToTheAlertEndpoints() throws Exception {
    var sensorId = UUID.fromString("10000000-0000-0000-0000-000000000001");
    var alert = new AlertDto(
        sensorId,
        "Created through the API",
        LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    );
    mockMvc.perform(post("/alerts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(alert)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sensorId").value(sensorId.toString()))
        .andExpect(jsonPath("$.message").value("Created through the API"));

    mockMvc.perform(get("/alerts/latest").queryParam("sensorId", sensorId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Created through the API"));
  }

  @Test
  void returnsMeasurementValuesBySensorTypeInTimestampOrderWithinInclusiveBounds() {
    var sensor = monitoringStore.saveSensor(
        new Sensor("ordered-values-" + UUID.randomUUID(), SensorType.HUMIDITY));
    var from = LocalDateTime.parse("2020-02-03T10:00:00");
    var middle = from.plusMinutes(1);
    var to = from.plusMinutes(2);
    monitoringStore.saveReading(new SensorReading(0.42, to, sensor));
    monitoringStore.saveReading(new SensorReading(0.46, from, sensor));
    monitoringStore.saveReading(new SensorReading(0.44, middle, sensor));
    monitoringStore.saveReading(
        new SensorReading(0.99, from.minusNanos(1), sensor));

    org.junit.jupiter.api.Assertions.assertEquals(
        java.util.List.of(0.46, 0.44, 0.42),
        measurementService.getMeasurementValuesBySensorType(SensorType.HUMIDITY, from, to)
    );
  }
}
