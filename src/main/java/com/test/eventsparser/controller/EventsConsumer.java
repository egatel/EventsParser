package com.test.eventsparser.controller;

import java.util.function.Consumer;

import com.test.eventsparser.EventsParserException;
import com.test.eventsparser.model.Event;

public interface EventsConsumer extends Consumer<Event> {
	public boolean start() throws EventsParserException;
	public void stop() throws EventsParserException;
}
