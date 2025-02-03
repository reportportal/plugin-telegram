package com.epam.reportportal.extension.telegram.event.handler;

/**
 * @author Andrei Piankouski
 */
public interface EventHandler<T> {

	void handle(T event);
}
