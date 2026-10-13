package com.aldisued.iot.monitoring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

  // Uses the application ObjectMapper so timestamps are ISO strings, like in the REST API.
  @Bean
  public DefaultKafkaProducerFactoryCustomizer jsonValueSerializerCustomizer(
      ObjectMapper objectMapper) {
    return factory -> {
      var serializer = new JsonSerializer<Object>(objectMapper);
      serializer.setAddTypeInfo(false);
      @SuppressWarnings("unchecked")
      var typedFactory = (DefaultKafkaProducerFactory<Object, Object>) factory;
      typedFactory.setValueSerializerSupplier(() -> serializer);
    };
  }

  // Converts JSON string payloads to the parameter type of each @KafkaListener method.
  @Bean
  public RecordMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
    return new StringJsonMessageConverter(objectMapper);
  }
}
