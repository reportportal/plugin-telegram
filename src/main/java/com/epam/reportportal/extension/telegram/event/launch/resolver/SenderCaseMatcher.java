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

import static com.epam.reportportal.extension.telegram.model.enums.template.StatisticTemplateProperty.STATISTIC_DEFECTS_PRODUCT_BUG;
import static com.epam.reportportal.extension.telegram.model.enums.template.StatisticTemplateProperty.STATISTIC_DEFECTS_SYSTEM_ISSUE;
import static com.epam.reportportal.extension.telegram.model.enums.template.StatisticTemplateProperty.STATISTIC_DEFECTS_TO_INVESTIGATE;
import static com.epam.reportportal.extension.telegram.model.enums.template.StatisticTemplateProperty.STATISTIC_EXECUTION_TOTAL;

import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogicalOperator;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.SendCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchStatistics;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.email.LaunchAttributeRule;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.email.SenderCase;
import com.epam.reportportal.base.reporting.AttributeResource;
import com.epam.reportportal.extension.telegram.utils.NotificationConfigConverter;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class SenderCaseMatcher {

  private final TestItemRepository testItemRepository;

  public SenderCaseMatcher(TestItemRepository testItemRepository) {
    this.testItemRepository = testItemRepository;
  }

  public boolean isSenderCaseMatched(SenderCase senderCase, Launch launch) {
    return isSuccessRateEnough(launch, senderCase.getSendCase())
        && isLaunchNameMatched(launch, senderCase)
        && isAttributesMatched(launch, senderCase.getLaunchAttributeRules(),
        senderCase.getAttributesOperator());
  }

  private static boolean isLaunchNameMatched(Launch launch, SenderCase oneCase) {
    Set<String> configuredNames = oneCase.getLaunchNames();
    return (null == configuredNames) || (configuredNames.isEmpty()) || configuredNames.contains(
        launch.getName());
  }

  private boolean isSuccessRateEnough(Launch launch, SendCase option) {
    switch (option) {
      case ALWAYS:
        return true;
      case FAILED:
        return testItemRepository.hasItemsWithIssueByLaunch(launch.getId());
      case TO_INVESTIGATE:
        return extractStatisticsCount(STATISTIC_DEFECTS_TO_INVESTIGATE.getName(),
            launch.getStatistics()) > 0;
      case MORE_10:
        return getSuccessRate(launch) > 0.1;
      case MORE_20:
        return getSuccessRate(launch) > 0.2;
      case MORE_50:
        return getSuccessRate(launch) > 0.5;
      default:
        return false;
    }
  }

  private static double getSuccessRate(Launch launch) {
    double ti =
        extractStatisticsCount(STATISTIC_DEFECTS_TO_INVESTIGATE.getName(),
            launch.getStatistics()).doubleValue();
    double pb =
        extractStatisticsCount(STATISTIC_DEFECTS_PRODUCT_BUG.getName(),
            launch.getStatistics()).doubleValue();
    double si =
        extractStatisticsCount(STATISTIC_DEFECTS_SYSTEM_ISSUE.getName(),
            launch.getStatistics()).doubleValue();
    double ab =
        extractStatisticsCount(STATISTIC_DEFECTS_PRODUCT_BUG.getName(),
            launch.getStatistics()).doubleValue();
    double total = extractStatisticsCount(STATISTIC_EXECUTION_TOTAL.getName(),
        launch.getStatistics()).doubleValue();
    return total == 0 ? total : (ti + pb + si + ab) / total;
  }

  public static Integer extractStatisticsCount(String statisticsField,
      Set<LaunchStatistics> statistics) {
    return statistics.stream()
        .filter(it -> it.getStatisticsField().getName().equalsIgnoreCase(statisticsField))
        .findFirst()
        .orElse(new LaunchStatistics())
        .getCounter();
  }

  private static boolean isAttributesMatched(Launch launch,
      Set<LaunchAttributeRule> launchAttributeRules, LogicalOperator logicalOperator) {

    if (CollectionUtils.isEmpty(launchAttributeRules)) {
      return true;
    }

    Set<AttributeResource> attributesResource =
        launchAttributeRules.stream().map(NotificationConfigConverter.TO_ATTRIBUTE_RULE_RESOURCE)
            .collect(Collectors.toSet());

    Set<AttributeResource> attributes =
        launch.getAttributes().stream().filter(attribute -> !attribute.isSystem())
            .map(attribute -> {
              AttributeResource attributeResource = new AttributeResource();
              attributeResource.setKey(attribute.getKey());
              attributeResource.setValue(attribute.getValue());
              return attributeResource;
            }).collect(Collectors.toSet());

    if (LogicalOperator.AND.equals(logicalOperator)) {
      return attributesResource.stream().allMatch(resourceAttr -> attributes.stream()
          .anyMatch(attr -> areAttributesMatched(attr, resourceAttr)));
    }

    return attributes.stream().anyMatch(attr -> attributesResource.stream()
        .anyMatch(resourceAttr -> areAttributesMatched(attr, resourceAttr)));
  }

  private static boolean areAttributesMatched(AttributeResource attribute,
      AttributeResource attributeResource) {
    // Case 1: Key and Value are the same
    boolean isEqual =
        Objects.equals(attribute.getKey(), attributeResource.getKey()) && Objects.equals(
            attribute.getValue(), attributeResource.getValue());

    // Case 2: Key is null in attributesResource and the Value is the same
    boolean isValueEqualWithKeyNull =
        attributeResource.getKey() == null && Objects.equals(attribute.getValue(),
            attributeResource.getValue()
        );

    return isEqual || isValueEqualWithKeyNull;
  }

}
