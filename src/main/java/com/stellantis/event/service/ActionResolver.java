package com.stellantis.event.service;

import java.util.List;

public class ActionResolver {

	public static List<String> resolve(String status) {
		return switch (status) {
		case "IN_PROGRESS" -> List.of();
		case "FINISHED" -> List.of("VALIDATE", "FREE_CONTRACTS");
		case "VALIDATED" -> List.of("CONFIRM", "CANCEL_VALIDATION");
		case "CONFIRMED" -> List.of("SEND_TO_PARTNER");
		case "CANCELLED" -> List.of();
		default -> List.of();
		};
	}

}
