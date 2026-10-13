package com.aldisued.iot.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
public class KafkaConfig {

  // Converts JSON string payloads to the parameter type of each @KafkaListener method.
  @Bean
  public RecordMessageConverter jsonMessageConverter() {
    return new StringJsonMessageConverter();
  }
}
