package com.epam.reportportal.extension.telegram.model.enums.template;

import com.epam.reportportal.extension.telegram.model.template.TemplateProperty;

public enum DefaultTemplateProperty implements TemplateProperty {

  LAUNCH_ID,
  LAUNCH_UUID,
  LAUNCH_NAME,
  LAUNCH_NUMBER,
  LAUNCH_START_TIME,
  LAUNCH_FINISH_TIME,
  LAUNCH_MODE,
  LAUNCH_DESCRIPTION,
  LAUNCH_ATTRIBUTES,

  LAUNCH_LINK;

  @Override
  public String getName() {
    return name();
  }
}
