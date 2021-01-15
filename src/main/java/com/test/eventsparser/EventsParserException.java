package com.test.eventsparser;

public class EventsParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4042078620114973361L;

	public EventsParserException() {
	}

	public EventsParserException(String message) {
		super(message);
	}

	public EventsParserException(Throwable cause) {
		super(cause);
	}

	public EventsParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventsParserException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
