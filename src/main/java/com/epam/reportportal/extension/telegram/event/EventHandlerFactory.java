package com.epam.reportportal.extension.telegram.event;

import com.epam.reportportal.extension.telegram.event.handler.EventHandler;

/**
 * @author Andrei Piankouski
 */
public interface EventHandlerFactory<T> {

  EventHandler<T> getEventHandler(String key);
}
