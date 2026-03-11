package com.stellantis.event.exception;

public class FileUnavailableException extends RuntimeException {
	private static final long serialVersionUID = -2780073126329349276L;

	public FileUnavailableException(String message) {
		super(message);
	}

	public FileUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
