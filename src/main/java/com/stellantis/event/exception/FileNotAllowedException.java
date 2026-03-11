package com.stellantis.event.exception;

public class FileNotAllowedException extends RuntimeException {
	private static final long serialVersionUID = 5789201255370465422L;

	public FileNotAllowedException(String message) {
        super(message);
    }
}