package com.epam.reportportal.extension.telegram.event.plugin;

import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.telegram.event.EventHandlerFactory;
import com.epam.reportportal.extension.telegram.event.handler.EventHandler;
import com.epam.reportportal.extension.telegram.event.handler.plugin.PluginLoadedEventHandler;
import com.epam.reportportal.extension.telegram.info.PluginInfoProvider;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Piankouski
 */
public class PluginEventHandlerFactory implements EventHandlerFactory<PluginEvent> {

  public static final String LOAD_KEY = "load";

  private final Map<String, EventHandler<PluginEvent>> eventHandlerMapping;

  public PluginEventHandlerFactory(IntegrationTypeRepository integrationTypeRepository,
       PluginInfoProvider pluginInfoProvider) {
    this.eventHandlerMapping = new HashMap<>();
    this.eventHandlerMapping.put(LOAD_KEY,
        new PluginLoadedEventHandler(integrationTypeRepository, pluginInfoProvider)
    );
  }

  @Override
  public EventHandler<PluginEvent> getEventHandler(String key) {
    return eventHandlerMapping.get(key);
  }
}
