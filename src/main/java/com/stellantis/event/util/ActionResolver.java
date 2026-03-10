package com.stellantis.event.util;

import java.time.LocalDate;
import java.util.List;

public final class ActionResolver {

	private ActionResolver() {
	}

    /**
     * Compute actions based on event status and additional conditions.
     *
     * @param status     Workflow status of the event
     * @param startDate  The processing start date of the event (used for same-day VALIDATE rule)
     * @return List of available action identifiers
     */
    public static List<String> resolve(String status, LocalDate startDate) {

        if (status == null) {
            return List.of();
        }

        switch (status) {

            case "IN_PROGRESS":
                // Job running — no actions available
                return List.of();

            case "FINISHED":
                boolean sameDay = startDate != null && startDate.equals(LocalDate.now());
                if (sameDay) {
                    return List.of("VALIDATE", "FREE_CONTRACTS");
                }
                return List.of("FREE_CONTRACTS");

            case "VALIDATED":
                return List.of("CONFIRM", "CANCEL_VALIDATION");

            case "CONFIRMED":
                return List.of("SEND_TO_PARTNER");

            case "CANCELLED":
            default:
                return List.of();
        }
    }


}
