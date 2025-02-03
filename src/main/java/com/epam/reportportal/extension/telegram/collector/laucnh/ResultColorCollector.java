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
package com.epam.reportportal.extension.telegram.collector.laucnh;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.INTERRUPTED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.PASSED;

import com.epam.reportportal.extension.telegram.collector.PropertyCollector;
import com.epam.reportportal.extension.telegram.model.enums.template.Color;
import com.epam.reportportal.extension.telegram.model.template.TextProperty;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class ResultColorCollector implements PropertyCollector<Launch, TextProperty> {

  private static final String RESULT_COLOR = "RESULT_COLOR";

  private final Map<StatusEnum, Color> colorMapping;

  public ResultColorCollector() {
    this.colorMapping = initMapping();
  }

  @Override
  public Map<TextProperty, String> collect(Launch launch) {
    Color color = colorMapping.getOrDefault(launch.getStatus(), Color.FAILED);
    return Collections.singletonMap(new TextProperty(RESULT_COLOR), color.getValue());
  }

  private Map<StatusEnum, Color> initMapping() {
    Map<StatusEnum, Color> mapping = new LinkedHashMap<>();
    mapping.put(PASSED, Color.PASSED);
    mapping.put(FAILED, Color.FAILED);
    mapping.put(INTERRUPTED, Color.INTERRUPTED);
    return mapping;
  }
}
