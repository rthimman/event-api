package com.stellantis.event.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stellantis.event.dto.FundEventSummaryDto;

public interface FundEventRepositoryCustom {

	Page<FundEventSummaryDto> searchEvents(String fundCode, String eventType, String status, LocalDateTime fromDate,
			LocalDateTime toDate, Pageable pageable);

}
