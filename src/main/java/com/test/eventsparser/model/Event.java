package com.test.eventsparser.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity 
public class Event {
	
    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;
    
    private Long    duration;
    private String  type;
    private String  host;
    private Boolean alert;  
    
    protected Event() {
    }
    
    public Event(String id, Long duration, String type, String host, Boolean alert) {
    	this.id       = id;
    	this.duration = duration;
    	this.type     = type;
    	this.host     = host;
    	this.alert    = alert;
    }
    
    
	@Override
	public String toString() {
		return String.format(
				"Event [id=%s, duration=%s, type=%s, host=%s, alert=%s]", 
				getId(), getDuration(), getType(), getHost(), getAlert());
	}

	public String getId() {
		return id;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
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

	public Boolean getAlert() {
		return alert;
	}

	public void setAlert(Boolean alert) {
		this.alert = alert;
	}	
}
