package com.aldisued.iot.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

/** Runs the Kafka paths of the application against a real broker started by Testcontainers. */
@Testcontainers
class KafkaMessagingTests extends IntegrationTestBase {

  @Container
  @ServiceConnection
  static final ConfluentKafkaContainer KAFKA =
      new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

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

  @Autowired
  private KafkaListenerEndpointRegistry listenerRegistry;

  private Sensor sensor;

  @BeforeEach
  void setUp() {
    listenerRegistry.getListenerContainers().forEach(container ->
        Awaitility.await().atMost(Duration.ofSeconds(60))
            .until(() -> !container.getAssignedPartitions().isEmpty()));
    sensor = sensorRepository.save(new Sensor("kafka-" + UUID.randomUUID(), SensorType.TEMPERATURE));
  }

  @AfterEach
  void tearDown() {
    alertRepository.deleteAll();
    sensorReadingRepository.deleteAll();
    sensorRepository.deleteAll();
  }

  @Test
  void readingPublishedToSensorReadingTopicIsPersisted() {
    send("sensor-reading", """
        {"sensorId":"%s","value":23.5,"timestamp":"2026-10-13T10:30:00"}
        """.formatted(sensor.getId()));

    Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
        assertThat(sensorReadingRepository.findValues(SensorType.TEMPERATURE,
            java.time.LocalDateTime.parse("2026-10-13T10:30:00"),
            java.time.LocalDateTime.parse("2026-10-13T10:30:00")))
            .containsExactly(23.5));
  }

  @Test
  void alertPublishedToSensorAlertsTopicIsPersisted() {
    send("sensor-alerts", """
        {"sensorId":"%s","message":"From Kafka","timestamp":"2026-10-13T11:00:00"}
        """.formatted(sensor.getId()));

    Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
        assertThat(alertRepository.findFirstBySensorIdOrderByTimestampDescIdDesc(sensor.getId()))
            .get().extracting(alert -> alert.getMessage()).isEqualTo("From Kafka"));
  }

  @Test
  void alertCreatedThroughTheApiIsPublishedAsJsonToTheAlertsTopic() throws Exception {
    try (var consumer = newConsumer()) {
      consumer.subscribe(List.of("alerts"));

      mockMvc.perform(MockMvcRequestBuilders.post("/alerts")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"sensorId":"%s","message":"Published","timestamp":"2026-10-13T12:00:00"}
                  """.formatted(sensor.getId())))
          .andExpect(MockMvcResultMatchers.status().isOk());

      var payloads = new java.util.ArrayList<String>();
      Awaitility.await().atMost(Duration.ofSeconds(30)).until(() -> {
        consumer.poll(Duration.ofMillis(500)).forEach(record -> payloads.add(record.value()));
        return payloads.stream().anyMatch(payload -> payload.contains(sensor.getId().toString()));
      });

      // Alerts persisted by the other tests also land on this shared topic.
      JsonNode json = objectMapper.readTree(payloads.stream()
          .filter(payload -> payload.contains(sensor.getId().toString())).findFirst().orElseThrow());
      assertThat(json.get("sensorId").asText()).isEqualTo(sensor.getId().toString());
      assertThat(json.get("message").asText()).isEqualTo("Published");
      assertThat(json.get("timestamp").asText()).startsWith("2026-10-13T12:00");
    }
  }

  private void send(String topic, String json) {
    var producerFactory = new DefaultKafkaProducerFactory<String, String>(Map.of(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class));
    try {
      new KafkaTemplate<>(producerFactory).send(topic, json.strip()).join();
    } finally {
      producerFactory.destroy();
    }
  }

  private static KafkaConsumer<String, String> newConsumer() {
    return new KafkaConsumer<>(Map.of(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
        ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
  }
}
