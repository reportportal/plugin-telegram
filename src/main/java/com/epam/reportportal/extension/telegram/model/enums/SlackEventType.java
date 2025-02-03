package com.epam.reportportal.extension.telegram.model.enums;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.integration.Integration;
import java.util.Optional;

public enum SlackEventType {

  LAUNCH_FINISHED("launchFinished", "Launch finished");

  private final String value;
  private final String message;

  SlackEventType(String value, String message) {
    this.value = value;
    this.message = message;
  }

  public String getValue() {
    return value;
  }

  public String getMessage() {
    return message;
  }

  public Optional<String> get(Integration integration) {
    return ofNullable(integration.getParams()).flatMap(p -> ofNullable(p.getParams()))
        .flatMap(p -> ofNullable(p.get(value)).map(String::valueOf));
  }
}
