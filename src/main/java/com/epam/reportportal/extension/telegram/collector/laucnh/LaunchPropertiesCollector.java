package com.epam.reportportal.extension.telegram.collector.laucnh;

import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_DESCRIPTION;
import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_FINISH_TIME;
import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_ID;
import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_MODE;
import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_NAME;
import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_NUMBER;
import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_START_TIME;
import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_UUID;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.telegram.collector.PropertyCollector;
import com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class LaunchPropertiesCollector implements
    PropertyCollector<Launch, DefaultTemplateProperty> {

  private static final int FIELD_LENGTH_RESTRICTION = 1970;

  public static final Map<DefaultTemplateProperty, Function<Launch, Object>> FIELD_MAPPING = ImmutableMap.<DefaultTemplateProperty, Function<Launch, Object>>builder()
      .put(LAUNCH_ID, Launch::getId)
      .put(LAUNCH_UUID, Launch::getUuid)
      .put(LAUNCH_NAME, Launch::getName)
      .put(LAUNCH_NUMBER, Launch::getNumber)
      .put(LAUNCH_START_TIME, Launch::getStartTime)
      .put(LAUNCH_FINISH_TIME, Launch::getEndTime)
      .put(LAUNCH_MODE, Launch::getMode)
      .put(LAUNCH_DESCRIPTION, launch -> cutString(launch.getDescription()))
      .build();

  @Override
  public Map<DefaultTemplateProperty, String> collect(Launch launch) {
    return FIELD_MAPPING.entrySet()
        .stream()
        .collect(LinkedHashMap::new,
            (m, v) -> m.put(v.getKey(),
                ofNullable(v.getValue().apply(launch)).map(String::valueOf).orElse("")),
            LinkedHashMap::putAll
        );
  }

  private static String cutString(String s) {
    if (s != null && s.length() > FIELD_LENGTH_RESTRICTION) {
      return s.substring(0, FIELD_LENGTH_RESTRICTION) + "...";
    }
    return s;
  }
}
