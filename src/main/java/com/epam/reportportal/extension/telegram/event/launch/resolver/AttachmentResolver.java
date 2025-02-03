/*
 * Copyright 2024 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.reportportal.extension.telegram.event.launch.resolver;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.telegram.binary.MessageTemplateStore;
import com.epam.reportportal.extension.telegram.collector.PropertyCollector;
import com.epam.reportportal.extension.telegram.factory.PropertyCollectorFactory;
import com.epam.reportportal.extension.telegram.model.enums.SlackEventType;
import com.epam.reportportal.extension.telegram.model.template.TemplateProperty;
import com.epam.reportportal.extension.telegram.model.template.TextProperty;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class AttachmentResolver {

  private static final String LAUNCH_LINK = "LAUNCH_LINK";

  private final MessageTemplateStore messageTemplateStore;

  private final PropertyCollectorFactory propertyCollectorFactory;

  public AttachmentResolver(MessageTemplateStore messageTemplateStore,
      PropertyCollectorFactory propertyCollectorFactory) {
    this.messageTemplateStore = messageTemplateStore;
    this.propertyCollectorFactory = propertyCollectorFactory;
  }

  public String resolve(Launch launch, String launchLink) {
    File file = messageTemplateStore.get(SlackEventType.LAUNCH_FINISHED).get();
    String template;
    try {
      template = Files.readString(file.toPath());
      System.out.println("Template: " + template);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return mapLaunchPropertiesToTemplate(launch, template, launchLink);
  }

  public String mapLaunchPropertiesToTemplate(Launch launch, String template, String launchLink) {
    String result = template;
    List<PropertyCollector<Launch, ? extends TemplateProperty>> propertyCollectors = propertyCollectorFactory.getDefaultCollectors();
    Map<TemplateProperty, String> propertyMap = propertyCollectors.stream()
        .map(c -> c.collect(launch))
        .flatMap(m -> m.entrySet().stream())
        .collect(
            LinkedHashMap::new,
            (m, v) -> m.put(v.getKey(),
                ofNullable(v.getValue()).map(String::valueOf).orElse("")),
            LinkedHashMap::putAll
        );

    if (StringUtils.isNotEmpty(launchLink)) {
      propertyMap.put(new TextProperty(LAUNCH_LINK), launchLink);
    } else {
      result = result.replace("href=\"${LAUNCH_LINK}\"", "");
    }

    for (Map.Entry<TemplateProperty, String> entry : propertyMap.entrySet()) {
      String key = "${" + entry.getKey().getName() + "}";
      Object value = entry.getValue();
      result = result.replace(key, value != null ? value.toString() : "");
    }

    return result;
  }
}
