package com.test.eventsparser.data;

import java.util.Objects;


/**
 * This is a class that allows for keeping information about events.
 * 
 * @author egatel
 *
 */
public class EventItem implements Comparable<EventItem> {	
	public static final String ID    		= "id";
	public static final String STATE 		= "state";
	public static final String TYPE  		= "type";
	public static final String HOST  	  	= "host";
	public static final String TIMESTAMP 	= "timestamp";
	
	public static enum State { 
		STARTED, FINISHED;
	};
	
	private String  id;
	//private State   state;
	private String  type;
	private String  host;
	private boolean last = false;  
	
	private Long[]  timeStamps = new Long[State.values().length];
	
	private int hashCode = Integer.MIN_VALUE;
	
	public EventItem(String id, State state, Long timeStamp) {
		this.id = Objects.requireNonNull(id, "Id cannot be null!");
		
		Objects.requireNonNull(state, "State cannot be null!");
		this.timeStamps[state.ordinal()] = 
				Objects.requireNonNull(timeStamp, "timeStamp cannot be null!");
	}
	
	public EventItem(String id, State state, Long timeStamp, String type, String host) {
		this(id, state, timeStamp);
		
		this.type = type;
		this.host = host;
	}

	public String getId() {
		return id;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}
	
	public void setTimeStamp(State state, Long timeStamp) {
		Objects.requireNonNull(state, "State cannot be null!");
		this.timeStamps[state.ordinal()] = 
				Objects.requireNonNull(timeStamp, "timeStamp cannot be null!");
	}
	
	public Long getTimeStamp(State state) {
		Objects.requireNonNull(state, "State cannot be null!");
		return this.timeStamps[state.ordinal()];
	}

	public boolean isLast() {
		return last;
	}

	public void setLast() {
		this.last = true;
	}	
	
	@Override
	public int compareTo(EventItem other) {
		return this.getId().compareTo(other.getId());
	}
	
	@Override
	public int hashCode() {
		if (hashCode == Integer.MIN_VALUE) {
			hashCode = Objects.hash(id);//TODO Not completed just an idea
		}
		
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		return id.equals(obj);//TODO not completed just an idea
	}
	
	@Override
	public String toString() {
		Long tStarted = getTimeStamp(State.STARTED);
		Long tFinished = getTimeStamp(State.FINISHED);
		return String.format(
				"EventItem [id=%s, STARTED=%s, FINISHED=%s, type=%s, host=%s, last=%s]", 
				getId(), 
				(tStarted != null? tStarted.toString(): "null"), (tFinished != null? tFinished.toString(): "null"),
				getType(), getHost(), isLast());
	}
}
