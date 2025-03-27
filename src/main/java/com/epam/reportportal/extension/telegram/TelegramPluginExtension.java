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

package com.epam.reportportal.extension.telegram;

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.LaunchFinishedPluginEvent;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.telegram.binary.MessageTemplateStore;
import com.epam.reportportal.extension.telegram.event.launch.TelegramLaunchFinishEventListener;
import com.epam.reportportal.extension.telegram.event.launch.resolver.AttachmentResolver;
import com.epam.reportportal.extension.telegram.event.launch.resolver.SenderCaseMatcher;
import com.epam.reportportal.extension.telegram.event.plugin.PluginEventHandlerFactory;
import com.epam.reportportal.extension.telegram.event.plugin.PluginEventListener;
import com.epam.reportportal.extension.telegram.factory.PropertyCollectorFactory;
import com.epam.reportportal.extension.telegram.info.impl.PluginInfoProviderImpl;
import com.epam.reportportal.extension.telegram.utils.MemoizingSupplier;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.pf4j.Extension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.client.RestTemplate;

/**
 * @author Andrei Piankouski
 */
@Extension
public class TelegramPluginExtension implements ReportPortalExtensionPoint, DisposableBean {

  private static final String PLUGIN_ID = "telegram";
  public static final String BINARY_DATA_PROPERTIES_FILE_ID = "binary-data.properties";

  private static final String DOCUMENTATION_LINK_FIELD = "documentationLink";

  private static final String DOCUMENTATION_LINK = "https://reportportal.io/docs/plugins/Telegram";

  public static final String SCRIPTS_DIR = "scripts";

  private final Supplier<Map<String, PluginCommand>> pluginCommandMapping = new MemoizingSupplier<>(
      this::getCommands);

  private final Supplier<Map<String, CommonPluginCommand<?>>> commonPluginCommandMapping = new MemoizingSupplier<>(
      this::getCommonCommands);

  private final String resourcesDir;

  private final Supplier<ApplicationListener<PluginEvent>> pluginLoadedListener;

  private final Supplier<ApplicationListener<LaunchFinishedPluginEvent>> launchFinishEventListenerSupplier;

  private final Supplier<MessageTemplateStore> messageTemplateStoreSupplier;

  private final Supplier<AttachmentResolver> attachmentResolverSupplier;

  private final Supplier<SenderCaseMatcher> senderCaseMatcher;

  @Autowired
  private IntegrationTypeRepository integrationTypeRepository;

  @Autowired
  private IntegrationRepository integrationRepository;

  @Autowired
  private TestItemRepository testItemRepository;

  @Autowired
  private LaunchRepository launchRepository;

  @Autowired
  private ProjectRepository projectRepository;

  // @Autowired // uncomment for future release
  private final RestTemplate restTemplate = new RestTemplate();

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private DataSource dataSource;

  public TelegramPluginExtension(Map<String, Object> initParams) {
    resourcesDir = IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams)
        .map(String::valueOf).orElse("");

    messageTemplateStoreSupplier = new MemoizingSupplier<>(
        () -> new MessageTemplateStore(resourcesDir));

    pluginLoadedListener = new MemoizingSupplier<>(
        () -> new PluginEventListener(PLUGIN_ID,
            new PluginEventHandlerFactory(integrationTypeRepository,
                new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID)
            )
        ));

    senderCaseMatcher = new MemoizingSupplier<>(() -> new SenderCaseMatcher(testItemRepository));

    attachmentResolverSupplier = new MemoizingSupplier<>(() -> new AttachmentResolver(
        messageTemplateStoreSupplier.get(), new PropertyCollectorFactory()));

    launchFinishEventListenerSupplier = new MemoizingSupplier<>(
        () -> new TelegramLaunchFinishEventListener(projectRepository,
            launchRepository, senderCaseMatcher.get(), attachmentResolverSupplier.get(), restTemplate));
  }

  @PostConstruct
  public void createIntegration() throws IOException {
    initListeners();
    initScripts();
  }

  private void initListeners() {
    ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class
    );
    applicationEventMulticaster.addApplicationListener(pluginLoadedListener.get());
    applicationEventMulticaster.addApplicationListener(launchFinishEventListenerSupplier.get());
  }

  private void initScripts() throws IOException {
    try (Stream<Path> paths = Files.list(Paths.get(resourcesDir, SCRIPTS_DIR))) {
      FileSystemResource[] scriptResources =
          paths.sorted().map(FileSystemResource::new).toArray(FileSystemResource[]::new);
      ResourceDatabasePopulator resourceDatabasePopulator =
          new ResourceDatabasePopulator(scriptResources);
      resourceDatabasePopulator.execute(dataSource);
    }
  }

  @Override
  public void destroy() {
    removeListeners();
  }

  private void removeListeners() {
    ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class
    );
    applicationEventMulticaster.removeApplicationListener(pluginLoadedListener.get());
    applicationEventMulticaster.removeApplicationListener(launchFinishEventListenerSupplier.get());
  }

  @Override
  public Map<String, ?> getPluginParams() {
    Map<String, Object> params = new HashMap<>();
    params.put(ALLOWED_COMMANDS, new ArrayList<>(pluginCommandMapping.get().keySet()));
    params.put(DOCUMENTATION_LINK_FIELD, DOCUMENTATION_LINK);
    params.put(COMMON_COMMANDS, new ArrayList<>(commonPluginCommandMapping.get().keySet()));
    return params;
  }

  @Override
  public CommonPluginCommand getCommonCommand(String commandName) {
    return commonPluginCommandMapping.get().get(commandName);
  }

  @Override
  public PluginCommand getIntegrationCommand(String commandName) {
    return pluginCommandMapping.get().get(commandName);
  }

  private Map<String, PluginCommand> getCommands() {
    HashMap<String, PluginCommand> pluginCommands = new HashMap<>();
    return pluginCommands;
  }

  private Map<String, CommonPluginCommand<?>> getCommonCommands() {
    HashMap<String, CommonPluginCommand<?>> pluginCommands = new HashMap<>();
    return pluginCommands;
  }

  protected ObjectMapper configureObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    om.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    om.registerModule(new JavaTimeModule());
    om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return om;
  }
}
