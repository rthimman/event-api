package com.stellantis.event.exception;

public class EventNotFoundException extends RuntimeException {

	public EventNotFoundException(String eventId) {
		super("Event not found: " + eventId);
	}

}
