package com.stellantis.event.converter;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.stellantis.event.dto.EventActionResultDto;
import com.stellantis.event.entity.FundEventEntity;
@Component
public class EventActionConverter {

	public EventActionResultDto toResult(FundEventEntity event, String action, String previousStatus, String newStatus,
			String executedBy, OffsetDateTime executedAt, String message) {

		EventActionResultDto dto = new EventActionResultDto();
		dto.setEventId(event.getId());
		dto.setFundCode(event.getFundEntity().getFundCode());
		dto.setAction(action);
		dto.setPreviousStatus(previousStatus);
		dto.setNewStatus(newStatus);
		dto.setExecutedBy(executedBy);
		dto.setExecutedAt(executedAt);
		dto.setMessage(message);
		return dto;
	}

}
