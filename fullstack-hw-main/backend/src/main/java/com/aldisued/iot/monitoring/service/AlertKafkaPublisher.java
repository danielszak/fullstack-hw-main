package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.AlertDto;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"kafka", "docker"})
public class AlertKafkaPublisher {

  private final KafkaTemplate<String, AlertDto> kafkaTemplate;

  public AlertKafkaPublisher(KafkaTemplate<String, AlertDto> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publish(AlertDto alertDto) {
    kafkaTemplate.send("alerts", alertDto);
  }
}
