package com.epam.reportportal.extension.telegram.model.enums.template;

import com.epam.reportportal.extension.telegram.model.template.TemplateProperty;

public enum StatisticTemplateProperty implements TemplateProperty {

  STATISTIC_EXECUTION_TOTAL("statistics$executions$total"),
  STATISTIC_EXECUTION_PASSED("statistics$executions$passed"),
  STATISTIC_EXECUTION_FAILED("statistics$executions$failed"),
  STATISTIC_EXECUTION_SKIPPED("statistics$executions$skipped"),
  STATISTIC_DEFECTS_PRODUCT_BUG("statistics$defects$product_bug$total"),
  STATISTIC_DEFECTS_SYSTEM_ISSUE ("statistics$defects$system_issue$total"),
  STATISTIC_DEFECTS_AUTOMATION_BUG("statistics$defects$automation_bug$total"),
  STATISTIC_DEFECTS_NO_DEFECT("statistics$defects$no_defect$total"),
  STATISTIC_DEFECTS_TO_INVESTIGATE("statistics$defects$to_investigate$total"),

  UNKNOWN("unknown");

  private final String statisticField;

  StatisticTemplateProperty(String statisticField) {
    this.statisticField = statisticField;
  }

  @Override
  public String getName() {
    return statisticField;
  }

  public static StatisticTemplateProperty valueOfByName(String name) {
    for (StatisticTemplateProperty prop : StatisticTemplateProperty.values()) {
      if (prop.getName().equals(name)) {
        return prop;
      }
    }
    return UNKNOWN;
  }
}
