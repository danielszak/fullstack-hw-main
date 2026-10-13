package com.aldisued.iot.monitoring.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SensorNameConflictException extends RuntimeException {

  public SensorNameConflictException(String name) {
    super("A sensor named '" + name + "' already exists.");
  }
}
