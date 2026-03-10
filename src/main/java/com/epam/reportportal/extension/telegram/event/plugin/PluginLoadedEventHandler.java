package com.epam.reportportal.extension.telegram.event.plugin;

import com.epam.reportportal.base.core.events.domain.PluginUploadedEvent;
import com.epam.reportportal.extension.telegram.info.PluginInfoProvider;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import org.springframework.context.ApplicationListener;

/**
 * @author Andrei Piankouski
 */
public class PluginLoadedEventHandler implements ApplicationListener<PluginUploadedEvent> {

  private final String pluginId;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final PluginInfoProvider pluginInfoProvider;

  public PluginLoadedEventHandler(String pluginId,
      IntegrationTypeRepository integrationTypeRepository,
      PluginInfoProvider pluginInfoProvider) {
    this.pluginId = pluginId;
    this.integrationTypeRepository = integrationTypeRepository;
    this.pluginInfoProvider = pluginInfoProvider;
  }

  @Override
  public void onApplicationEvent(PluginUploadedEvent event) {
    if (!supports(event)) {
      return;
    }

    String eventPluginId = event.getPluginActivityResource().getName();
    integrationTypeRepository.findByName(eventPluginId).ifPresent(integrationType -> {
      integrationTypeRepository.save(pluginInfoProvider.provide(integrationType));
    });
  }

  private boolean supports(PluginUploadedEvent event) {
    return pluginId.equals(event.getPluginActivityResource().getName());
  }
}
