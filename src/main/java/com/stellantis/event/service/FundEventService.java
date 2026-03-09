package com.stellantis.event.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.stellantis.event.dto.FundEventSummaryDto;
import com.stellantis.event.exception.ApiException;
import com.stellantis.event.exception.ErrorCode;
import com.stellantis.event.repository.FundEventRepository;
import com.stellantis.event.repository.FundRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FundEventService {


	
    private final FundRepository fundRepository;
	private final FundEventRepository eventRepository;
    

public Page<FundEventSummaryDto> getEvents(
            String fundCode,
            String status,
            String eventType,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {

        // Fund existence check
        if (!fundRepository.existsByFundCode(fundCode)) {
            throw new ApiException(ErrorCode.FUND_NOT_FOUND, "Fund not found: " + fundCode);
        }

        // Validate date range
        if (from != null && to != null && from.isAfter(to)) {
            throw new ApiException(ErrorCode.INVALID_DATE_RANGE,
                    "startDateFrom must be before startDateTo");
        }

        LocalDateTime fromDate = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDate = to != null ? to.atTime(23, 59, 59) : null;

        Page<Object[]> raw = eventRepository.searchEvents(
                fundCode, eventType, status, fromDate, toDate, pageable);

        return raw.map(this::mapToDto);
    }

    private FundEventSummaryDto mapToDto(Object[] row) {
        UUID eventId = (UUID) row[0];
        String fundcode = (String) row[1];
        String eventType = (String) row[2];

        List<String> actions = ActionResolver.resolve(
                (String) row[4]  // STATUS
        );

        return new FundEventSummaryDto(
                eventId,
                fundcode,
                eventType,
                (String) row[3],
                (String) row[4],
                (LocalDateTime) row[5], //LocalDateTime
                (LocalDateTime) row[6],
                (BigDecimal) row[7],
                (Boolean) row[8],
                ((Number) row[10]).intValue(),
                ((Number) row[11]).intValue(),
                (String) row[9],
                actions
        );
    }
	
}
