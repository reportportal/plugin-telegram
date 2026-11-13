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
package com.epam.reportportal.extension.telegram.event.launch;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectUtils;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.email.SenderCase;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.event.LaunchFinishedNotificationEvent;
import com.epam.reportportal.extension.telegram.event.launch.resolver.AttachmentResolver;
import com.epam.reportportal.extension.telegram.event.launch.resolver.SenderCaseMatcher;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class TelegramLaunchFinishEventListener implements ApplicationListener<LaunchFinishedNotificationEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TelegramLaunchFinishEventListener.class);

  public final static String TELEGRAM_NOTIFICATION_ATTRIBUTE = "notifications.telegram.enabled";
  public final static String CHAT_ID = "chatId";

  public final static String API_KEY = "apiKey";

  public final static String PLUGIN_NOTIFICATION_TYPE = "telegram";

  private final ProjectRepository projectRepository;

  private final LaunchRepository launchRepository;

  private final SenderCaseMatcher senderCaseMatcher;

  private final AttachmentResolver attachmentResolver;
  private final RestClient restClient;


  public TelegramLaunchFinishEventListener(
      ProjectRepository projectRepository, LaunchRepository launchRepository,
      SenderCaseMatcher senderCaseMatcher, AttachmentResolver attachmentResolver,
      RestClient restClient) {
    this.projectRepository = projectRepository;
    this.launchRepository = launchRepository;
    this.senderCaseMatcher = senderCaseMatcher;
    this.attachmentResolver = attachmentResolver;
    this.restClient = restClient;
  }

  @Override
  public void onApplicationEvent(LaunchFinishedNotificationEvent event) {
    try {
      Project project = getProject(event.getProjectId());
      if (isNotificationsEnabled(project)) {
        Launch launch = getLaunch(event.getLaunchId());
        processSenderCases(project, launch, event.getLaunchLink());
      }
    } catch (Exception e) {
      LOGGER.error("Failed to process Telegram notification for launch");
    }
  }

  private Project getProject(Long projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
  }

  private Launch getLaunch(Long launchId) {
    return launchRepository.findById(launchId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
  }

  private void processSenderCases(Project project, Launch launch, String launchLink) {
    project.getSenderCases().stream()
        .filter(SenderCase::isEnabled)
        .filter(this::isSlackSenderCase)
        .forEach(senderCase -> processSenderCase(senderCase, launch, launchLink));
  }

  private boolean isSlackSenderCase(SenderCase senderCase) {
    return senderCase.getType().equals(PLUGIN_NOTIFICATION_TYPE);
  }

  private void processSenderCase(SenderCase senderCase, Launch launch, String launchLink) {
    if (senderCaseMatcher.isSenderCaseMatched(senderCase, launch)) {
      sendNotification(senderCase, launch, launchLink);
    }
  }

  private void sendNotification(SenderCase senderCase, Launch launch, String launchLink) {
    Optional<String> chatId = getChatId(senderCase);
    Optional<String> apiKey = getApiKey(senderCase);
    Optional<String> attachment = resolveAttachment(launch, launchLink);
    if (chatId.isPresent() && apiKey.isPresent() && attachment.isPresent()) {
      sendMessage(apiKey.get(), Long.parseLong(chatId.get()), attachment.get());
    }
  }

  public void sendMessage(String apiKey, Long chatId, String msg) {
    String baseUrl = "https://api.telegram.org/bot" + apiKey + "/sendMessage";

    Map<String, Object> body = new HashMap<>();
    body.put("chat_id", chatId);
    body.put("text", msg);
    body.put("parse_mode", "HTML");

    restClient.post()
        .uri(baseUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .toBodilessEntity();
    LOGGER.debug("Telegram message sent to chat {}", chatId);
  }

  private Optional<String> getChatId(SenderCase senderCase) {
    return Optional.ofNullable(
        (String) senderCase.getRuleDetails().getOptions().get(CHAT_ID));
  }

  private Optional<String> getApiKey(SenderCase senderCase) {
    return Optional.ofNullable(
        (String) senderCase.getRuleDetails().getOptions().get(API_KEY));
  }

  private Optional<String> resolveAttachment(Launch launch, String launchLink) {
    String resolve = attachmentResolver.resolve(launch, launchLink);
    return Optional.of(resolve);
  }


  private boolean isNotificationsEnabled(Project project) {
    Map<String, String> projectConfig = ProjectUtils.getConfigParameters(
        project.getProjectAttributes());
    return BooleanUtils.toBoolean(
        projectConfig.get(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute()))
        && BooleanUtils.toBoolean(projectConfig.get(TELEGRAM_NOTIFICATION_ATTRIBUTE));
  }
}
