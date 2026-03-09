package com.stellantis.event.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	FUND_NOT_FOUND(HttpStatus.NOT_FOUND),
	INVALID_EVENT_TYPE(HttpStatus.BAD_REQUEST),
	INVALID_STATUS(HttpStatus.BAD_REQUEST),
	INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST),
	INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST), 
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED), 
	FORBIDDEN(HttpStatus.FORBIDDEN),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

	public final HttpStatus httpStatus;

	ErrorCode(HttpStatus status) {
		this.httpStatus = status;
	}

}
