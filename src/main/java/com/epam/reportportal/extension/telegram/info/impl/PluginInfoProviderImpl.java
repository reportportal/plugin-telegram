package com.epam.reportportal.extension.telegram.info.impl;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.telegram.info.PluginInfoProvider;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PluginInfoProviderImpl implements PluginInfoProvider {

  private static final String BINARY_DATA_KEY = "binaryData";
  private static final String DESCRIPTION_KEY = "description";

  private static final String FIELDS_KEY = "ruleFields";

  private static final String RULE_DESCRIPTION_KEY = "ruleDescription";
  private static final String METADATA_KEY = "metadata";

  private static final String PLUGIN_DESCRIPTION =
      "Reinforce your ReportPortal instance with Telegram integration. Be informed about test result finish in real time in your Telegram channel.";

  private static final String RULE_DESCRIPTION =
      "Select Telegram channel for every rule to send launch related notifications";
  public static final Map<String, Object> PLUGIN_METADATA = new HashMap<>();

  static {
    PLUGIN_METADATA.put("isIntegrationsAllowed", false);
  }

  private final String resourcesDir;
  private final String propertyFile;

  public PluginInfoProviderImpl(String resourcesDir, String propertyFile) {
    this.resourcesDir = resourcesDir;
    this.propertyFile = propertyFile;
  }

  @Override
  public IntegrationType provide(IntegrationType integrationType) {
    integrationType.setIntegrationGroup(IntegrationGroupEnum.NOTIFICATION);
    loadBinaryDataInfo(integrationType);
    updateDescription(integrationType);
    updateMetadata(integrationType);
    addFieldsInfo(integrationType);
    addRuleDescriptionInfo(integrationType);
    return integrationType;
  }

  private void loadBinaryDataInfo(IntegrationType integrationType) {
    Map<String, Object> details = integrationType.getDetails().getDetails();
    if (ofNullable(details.get(BINARY_DATA_KEY)).isEmpty()) {
      try (InputStream propertiesStream = Files.newInputStream(
          Paths.get(resourcesDir, propertyFile))) {
        Properties binaryDataProperties = new Properties();
        binaryDataProperties.load(propertiesStream);
        Map<String, String> binaryDataInfo = binaryDataProperties.entrySet()
            .stream()
            .collect(HashMap::new,
                (map, entry) -> map.put(String.valueOf(entry.getKey()),
                    String.valueOf(entry.getValue())),
                HashMap::putAll
            );
        details.put(BINARY_DATA_KEY, binaryDataInfo);
      } catch (IOException ex) {
        throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, ex.getMessage());
      }
    }
  }

  private void updateDescription(IntegrationType integrationType) {
    Map<String, Object> details = integrationType.getDetails().getDetails();
    details.put(DESCRIPTION_KEY, PLUGIN_DESCRIPTION);
  }

  private void updateMetadata(IntegrationType integrationType) {
    Map<String, Object> details = integrationType.getDetails().getDetails();
    details.put(METADATA_KEY, PLUGIN_METADATA);
  }

  private void addRuleDescriptionInfo(IntegrationType integrationType) {
    Map<String, Object> details = integrationType.getDetails().getDetails();
    details.put(RULE_DESCRIPTION_KEY, RULE_DESCRIPTION);
  }

  private void addFieldsInfo(IntegrationType integrationType) {
    Map<String, Object> details = integrationType.getDetails().getDetails();
    Map<String, Object> chatField = new HashMap<>();
    chatField.put("name", "chatId");
    chatField.put("label", "Chat id");
    chatField.put("type", "text");
    chatField.put("required", true);
    Map<String, Object> apiKeyField = new HashMap<>();
    apiKeyField.put("name", "apiKey");
    apiKeyField.put("label", "Bot Api Key");
    apiKeyField.put("type", "text");
    apiKeyField.put("required", true);
    details.put(FIELDS_KEY, List.of(chatField, apiKeyField));
  }
}
