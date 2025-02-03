package com.epam.reportportal.extension.telegram.binary;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonObjectLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonObjectLoader.class);

  private final ObjectMapper objectMapper;

  public JsonObjectLoader(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> Optional<T> load(File file, Class<T> clazz) {
    try {
      return Optional.ofNullable(objectMapper.readValue(FileUtils.openInputStream(file), clazz));
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

}
