package com.aldisued.iot.monitoring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Exercises the documented REST contract against PostgreSQL started by Testcontainers. */
class RestApiTests extends IntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SensorRepository sensorRepository;

  @Autowired
  private SensorReadingRepository sensorReadingRepository;

  @Autowired
  private AlertRepository alertRepository;

  @MockitoBean
  private KafkaTemplate<String, AlertDto> kafkaTemplate;

  @AfterEach
  void tearDown() {
    alertRepository.deleteAll();
    sensorReadingRepository.deleteAll();
    sensorRepository.deleteAll();
  }

  @Test
  void sensorsAreListedByNameWithoutRelationFields() throws Exception {
    createSensor("b-sensor", "HUMIDITY");
    createSensor("a-sensor", "ATMOSPHERIC_PRESSURE");

    mockMvc.perform(get("/sensors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("a-sensor"))
        .andExpect(jsonPath("$[0].type").value("ATMOSPHERIC_PRESSURE"))
        .andExpect(jsonPath("$[0].id").exists())
        .andExpect(jsonPath("$[0].alerts").doesNotExist())
        .andExpect(jsonPath("$[0].sensorReadings").doesNotExist())
        .andExpect(jsonPath("$[1].name").value("b-sensor"));
  }

  @Test
  void invalidSensorsAreRejected() throws Exception {
    postJson("/sensors", "{\"name\":\"   \",\"type\":\"TEMPERATURE\"}")
        .andExpect(status().isBadRequest());
    postJson("/sensors", "{\"name\":\"no-type\"}").andExpect(status().isBadRequest());
    postJson("/sensors", "{\"name\":\"bad-type\",\"type\":\"WIND\"}")
        .andExpect(status().isBadRequest());

    createSensor("unique", "TEMPERATURE");
    postJson("/sensors", "{\"name\":\"unique\",\"type\":\"HUMIDITY\"}")
        .andExpect(status().isConflict());
  }

  @Test
  void readingsAreSavedAndListedWithTheirSensorId() throws Exception {
    var sensorId = createSensor("reading-sensor", "TEMPERATURE");

    postJson("/sensor-readings", """
        {"sensorId":"%s","value":22.5,"timestamp":"2026-10-09T10:00"}
        """.formatted(sensorId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.sensorId").value(sensorId))
        .andExpect(jsonPath("$.value").value(22.5));

    mockMvc.perform(get("/sensor-readings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].sensorId").value(sensorId))
        .andExpect(jsonPath("$[0].timestamp").value("2026-10-09T10:00:00"));
  }

  @Test
  void invalidReadingsAreRejected() throws Exception {
    postJson("/sensor-readings", """
        {"sensorId":"%s","value":1.0,"timestamp":"2026-10-09T10:00:00"}
        """.formatted(UUID.randomUUID()))
        .andExpect(status().isNotFound());

    var sensorId = createSensor("reading-sensor", "TEMPERATURE");
    postJson("/sensor-readings", """
        {"sensorId":"%s","timestamp":"2026-10-09T10:00:00"}
        """.formatted(sensorId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void alertsAreListedNewestFirstAndLatestReturnsTheNewest() throws Exception {
    var sensorId = createSensor("alert-sensor", "TEMPERATURE");
    createAlert(sensorId, "older", "2026-10-13T08:00:00");
    createAlert(sensorId, "newest", "2026-10-13T10:00:00");
    createAlert(sensorId, "middle", "2026-10-13T09:00:00");

    mockMvc.perform(get("/alerts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].message").value("newest"))
        .andExpect(jsonPath("$[1].message").value("middle"))
        .andExpect(jsonPath("$[2].message").value("older"))
        .andExpect(jsonPath("$[0].sensorId").value(sensorId));

    mockMvc.perform(get("/alerts/latest").queryParam("sensorId", sensorId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("newest"))
        .andExpect(jsonPath("$.sensorId").value(sensorId));
  }

  @Test
  void latestAlertAndAlertCreationErrorsUseTheDocumentedStatusCodes() throws Exception {
    var sensorId = createSensor("quiet-sensor", "HUMIDITY");

    mockMvc.perform(get("/alerts/latest").queryParam("sensorId", sensorId))
        .andExpect(status().isNotFound());
    mockMvc.perform(get("/alerts/latest")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/alerts/latest").queryParam("sensorId", "not-a-uuid"))
        .andExpect(status().isBadRequest());

    postJson("/alerts", """
        {"sensorId":"%s","message":"x","timestamp":"2026-10-13T10:00:00"}
        """.formatted(UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  private String createSensor(String name, String type) throws Exception {
    var body = postJson("/sensors", "{\"name\":\"%s\",\"type\":\"%s\"}".formatted(name, type))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body).get("id").asText();
  }

  private void createAlert(String sensorId, String message, String timestamp) throws Exception {
    postJson("/alerts", """
        {"sensorId":"%s","message":"%s","timestamp":"%s"}
        """.formatted(sensorId, message, timestamp))
        .andExpect(status().isOk());
  }

  private org.springframework.test.web.servlet.ResultActions postJson(String url, String body)
      throws Exception {
    return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
  }
}
