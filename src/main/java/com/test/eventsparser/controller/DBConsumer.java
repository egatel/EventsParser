package com.test.eventsparser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.test.eventsparser.repository.EventRepository;
import com.test.eventsparser.model.Event;

/**
 * The Storage that saves data to the database.
 * 
 * @author egatel
 */
@Component
public class DBConsumer implements EventsConsumer {
	
    @Autowired
    private EventRepository eventRepository;
	
	@Override
	public void accept(Event event) {
		
		//save result to the database
		eventRepository.save(event);
		
		//TODO: what if there is same Id present already in the DB ?
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public void stop() {
	}
}
