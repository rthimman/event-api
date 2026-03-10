package com.stellantis.event.exception;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

	String errorCode;
	String message;
	int status;
	String path;
	OffsetDateTime timestamp;

}
