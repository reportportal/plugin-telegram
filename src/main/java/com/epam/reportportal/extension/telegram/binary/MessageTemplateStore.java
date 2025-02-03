package com.epam.reportportal.extension.telegram.binary;

import static com.epam.reportportal.extension.telegram.model.enums.SlackEventType.LAUNCH_FINISHED;

import com.epam.reportportal.extension.telegram.model.enums.SlackEventType;
import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public class MessageTemplateStore {

  private static final String MESSAGE_TEMPLATES_DIR = "message-template";

  private final String resourcesDir;

  private final Map<SlackEventType, File> templateMapping;

  public MessageTemplateStore(String resourcesDir) {
    this.resourcesDir = resourcesDir;
    this.templateMapping = initTemplateMapping();
  }

  public Optional<File> get(SlackEventType eventType) {
    return Optional.ofNullable(templateMapping.get(eventType));
  }

  private Map<SlackEventType, File> initTemplateMapping() {
    return Map.of(
        LAUNCH_FINISHED,
        getTemplateFile("template.txt"));
  }

  private File getTemplateFile(String file) {
    return Paths.get(resourcesDir, MESSAGE_TEMPLATES_DIR, file).toFile();
  }
}
