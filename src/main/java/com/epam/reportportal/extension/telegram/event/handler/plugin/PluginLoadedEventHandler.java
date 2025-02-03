package com.epam.reportportal.extension.telegram.event.handler.plugin;

import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.telegram.event.handler.EventHandler;
import com.epam.reportportal.extension.telegram.info.PluginInfoProvider;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;

/**
 * @author Andrei Piankouski
 */
public class PluginLoadedEventHandler implements EventHandler<PluginEvent> {

  private final IntegrationTypeRepository integrationTypeRepository;
  private final PluginInfoProvider pluginInfoProvider;

  public PluginLoadedEventHandler(IntegrationTypeRepository integrationTypeRepository,
      PluginInfoProvider pluginInfoProvider) {
    this.integrationTypeRepository = integrationTypeRepository;
    this.pluginInfoProvider = pluginInfoProvider;
  }

  @Override
  public void handle(PluginEvent event) {
    integrationTypeRepository.findByName(event.getPluginId()).ifPresent(integrationType -> {
      integrationTypeRepository.save(pluginInfoProvider.provide(integrationType));
    });
  }
}
