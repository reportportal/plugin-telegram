package com.epam.reportportal.extension.telegram.collector;

import com.epam.reportportal.extension.telegram.model.template.TemplateProperty;
import java.util.Map;

public interface PropertyCollector<E, P extends TemplateProperty> {

  Map<P, String> collect(E entity);

}
