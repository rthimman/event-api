package com.stellantis.event.exception;

public class FundNotFoundException extends RuntimeException{

	private static final long serialVersionUID = 3612441243283311045L;

	  public FundNotFoundException(String fundCode) {
	        super("Fund not found: " + fundCode);
	    }

}
