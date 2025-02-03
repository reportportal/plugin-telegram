package com.epam.reportportal.extension.telegram.collector.laucnh;

import static com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty.LAUNCH_ATTRIBUTES;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.telegram.collector.PropertyCollector;
import com.epam.reportportal.extension.telegram.model.enums.template.DefaultTemplateProperty;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

public class AttributesCollector implements PropertyCollector<Launch, DefaultTemplateProperty> {

  @Override
  public Map<DefaultTemplateProperty, String> collect(Launch launch) {
    return ofNullable(launch.getAttributes()).filter(CollectionUtils::isNotEmpty)
        .map(this::convertToProperties)
        .orElseGet(Collections::emptyMap);
  }

  private Map<DefaultTemplateProperty, String> convertToProperties(Set<ItemAttribute> attributes) {
    final String attributesString = attributes.stream()
        .filter(a -> BooleanUtils.isFalse(a.isSystem()))
        .map(a -> ofNullable(a.getKey()).orElse("") + ":" + a.getValue())
        .collect(Collectors.joining("; "));
    return Map.of(LAUNCH_ATTRIBUTES, attributesString);
  }
}
