package com.epam.reportportal.extension.telegram.model.enums;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.integration.Integration;
import java.util.Optional;

public enum SlackIntegrationProperties {

  TOKEN("token"),
  CHANNEL("channel"),
  APP_URL("appUrl");

  private final String key;

  SlackIntegrationProperties(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public Optional<String> get(Integration integration) {
    return ofNullable(integration.getParams()).flatMap(p -> ofNullable(p.getParams()))
        .flatMap(p -> ofNullable(p.get(this.key)).map(String::valueOf));
  }

  public void set(Integration integration, String value) {
    ofNullable(integration.getParams()).flatMap(p -> ofNullable(p.getParams()))
        .ifPresent(p -> p.put(this.key, value));
  }
}
