package com.stellantis.event.dto;

public enum ActionType {
	VALIDATE,
	CONFIRM,
	CANCEL_VALIDATION,
	FREE_CONTRACTS,
	SEND_TO_PARTNER;

	public static ActionType from(String value) {
		if (value == null)
			return null;
		for (ActionType a : values()) {
			if (a.name().equalsIgnoreCase(value))
				return a;
		}
		return null;
	}
}
