package com.test.eventsparser.controller;

import com.test.eventsparser.EventsParserException;
import com.test.eventsparser.data.EventItem;
import java.util.function.Consumer;

public interface EventsSupplier {
	public void addConsumer(Consumer<EventItem> consumer);
	public boolean start() throws EventsParserException;
	public boolean hasNext();
	public void stop() throws EventsParserException;
}
