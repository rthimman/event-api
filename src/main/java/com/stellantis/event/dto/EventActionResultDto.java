package com.stellantis.event.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventActionResultDto {
    private UUID eventId;
    private String fundCode;
    private String action;
    private String previousStatus;
    private String newStatus;
    private String executedBy;
    private OffsetDateTime executedAt;
    private String message; // optional info (e.g., path or note)
}
