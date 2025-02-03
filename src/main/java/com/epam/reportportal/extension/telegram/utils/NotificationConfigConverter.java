/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.extension.telegram.utils;

import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class NotificationConfigConverter {

  private NotificationConfigConverter() {
    //static only
  }

  public static final Function<LaunchAttributeRule, ItemAttributeResource>
      TO_ATTRIBUTE_RULE_RESOURCE = model -> {
    ItemAttributeResource attributeResource = new ItemAttributeResource();
    attributeResource.setKey(model.getKey());
    attributeResource.setValue(model.getValue());
    return attributeResource;
  };


}
