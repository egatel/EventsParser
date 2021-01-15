package com.test.eventsparser.data;

import java.util.HashMap;

import com.test.eventsparser.data.EventItem.State;

/**
 * A class that helps in creating a proper form of EventItem objects.
 * 
 * @author egatel
 *
 */
public class EventItemBuilder {
	private HashMap<String, Object> pieces = new HashMap<String, Object>();
	
	public EventItemBuilder add(String name, Object value) {
		if (name != null) {
			pieces.put(name, value);
		}
		
		return this;
	}
	
	public EventItem build() {
		String id 	    	= (String) pieces.get(EventItem.ID);
		State  state    	= (State) State.valueOf(pieces.get(EventItem.STATE).toString());
		Long   timeStamp	= (Long) pieces.get(EventItem.TIMESTAMP);
			
		String type         = pieces.get(EventItem.TYPE) != null ? (String) pieces.get(EventItem.TYPE) : null;
		String host         = pieces.get(EventItem.HOST) != null ? (String) pieces.get(EventItem.HOST) : null;
			
		return new EventItem(id, state, timeStamp, type, host);
	}
}
