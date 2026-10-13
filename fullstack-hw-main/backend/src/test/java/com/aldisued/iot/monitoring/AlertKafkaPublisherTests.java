package com.aldisued.iot.monitoring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.service.AlertKafkaPublisher;
import com.aldisued.iot.monitoring.service.AlertService;
import com.aldisued.iot.monitoring.service.MonitoringStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;

class AlertKafkaPublisherTests {

  @Test
  void savesAlertAndPublishesItsDtoToAlertsTopic() {
    @SuppressWarnings("unchecked")
    var kafkaTemplate = (KafkaTemplate<String, AlertDto>) mock(KafkaTemplate.class);
    var publisher = new AlertKafkaPublisher(kafkaTemplate);
    @SuppressWarnings("unchecked")
    var publisherProvider = (ObjectProvider<AlertKafkaPublisher>) mock(ObjectProvider.class);
    when(publisherProvider.getIfAvailable()).thenReturn(publisher);

    var store = new MonitoringStore();
    var sensor = store.findAllSensors().stream()
        .filter(candidate -> candidate.getType() == SensorType.TEMPERATURE)
        .findFirst()
        .orElseThrow();
    var alertDto = new AlertDto(sensor.getId(), "Test alert", LocalDateTime.now());

    var savedAlert = new AlertService(store, publisherProvider).saveAlert(alertDto);

    org.junit.jupiter.api.Assertions.assertEquals(alertDto.message(), savedAlert.getMessage());
    verify(kafkaTemplate).send("alerts", alertDto);
  }
}
