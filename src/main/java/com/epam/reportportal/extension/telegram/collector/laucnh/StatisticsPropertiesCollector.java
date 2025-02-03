package com.epam.reportportal.extension.telegram.collector.laucnh;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.telegram.collector.PropertyCollector;
import com.epam.reportportal.extension.telegram.model.enums.template.StatisticTemplateProperty;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class StatisticsPropertiesCollector implements PropertyCollector<Launch, StatisticTemplateProperty> {

  private static final String STATISTICS_DEFAULT_VALUE = "0";

  @Override
  public Map<StatisticTemplateProperty, String> collect(Launch launch) {
    Map<StatisticTemplateProperty, String> properties = Arrays.stream(StatisticTemplateProperty.values())
        .collect(Collectors.toMap(Function.identity(), v -> STATISTICS_DEFAULT_VALUE));

    ofNullable(launch.getStatistics()).filter(CollectionUtils::isNotEmpty)
        .ifPresent(statistics -> statistics.stream()
            .filter(s -> properties.containsKey(StatisticTemplateProperty.valueOfByName(s.getStatisticsField().getName())))
            .forEach(s -> properties.put(StatisticTemplateProperty.valueOfByName(s.getStatisticsField().getName()), String.valueOf(s.getCounter()))));

    return properties;
  }
}
