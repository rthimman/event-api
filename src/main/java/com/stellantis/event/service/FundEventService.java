package com.stellantis.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.stellantis.event.dto.FundEventSummaryDto;
import com.stellantis.event.dto.PageResponse;
import com.stellantis.event.exception.ApiException;
import com.stellantis.event.exception.ErrorCode;
import com.stellantis.event.repository.FundEventRepository;
import com.stellantis.event.repository.FundRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundEventService {

    private static final Set<String> ALLOWED_EVENT_TYPES = Set.of(
            "REPLENISHMENT", "MONTHLY_DAILY_BM", "MONTHLY_MONTHLY_BM", "DAILY"
    );

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "IN_PROGRESS", "FINISHED", "VALIDATED", "CONFIRMED", "CANCELLED"
    );

    private static final int MAX_PAGE_SIZE = 100;

    private final FundRepository fundRepository;
    private final FundEventRepository fundEventRepository;

    /**
     * Returns paginated event summaries for a given fund.
     *
     * @param fundCode      required fund business key
     * @param eventType     optional filter (must be in allowed set if provided)
     * @param status        optional filter (must be in allowed set if provided)
     * @param startDateFrom optional filter (inclusive, start of day)
     * @param startDateTo   optional filter (inclusive, end of day)
     * @param pageable      page + size + sort (default sort will be set at controller level)
     */
    @Transactional
    public PageResponse getEvents(
            String fundCode,
            String eventType,
            String status,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            Pageable pageable
    ) {
    	

        //  Validate enums 
        if (eventType != null && !ALLOWED_EVENT_TYPES.contains(eventType)) {
            throw new ApiException(ErrorCode.INVALID_EVENT_TYPE, "Invalid eventType: " + eventType);
        }
        if (status != null && !ALLOWED_STATUSES.contains(status)) {
            throw new ApiException(ErrorCode.INVALID_STATUS, "Invalid status: " + status);
        }
        
        // ---- Validate page size 
        int size = pageable.getPageSize();
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(ErrorCode.INVALID_PAGE_SIZE,
                    "Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        
        // Validate date range 
        if (startDateFrom != null && startDateTo != null && startDateFrom.isAfter(startDateTo)) {
            throw new ApiException(ErrorCode.INVALID_DATE_RANGE, "startDateFrom must be before startDateTo");
        }

        
        // Validate fund existence
        if (!fundRepository.existsByFundCode(fundCode)) {
            throw new ApiException(ErrorCode.FUND_NOT_FOUND, "Fund not found: " + fundCode);
        }


        // Convert dates to timestamps (inclusive range)
        LocalDateTime fromDate = startDateFrom != null ? startDateFrom.atStartOfDay() : null;
        LocalDateTime toDate = startDateTo != null ? startDateTo.atTime(23, 59, 59) : null;

        log.info("Searching events for fundCode='{}', eventType='{}', status='{}', from='{}', to='{}', page={}, size={}",
                fundCode, eventType, status, fromDate, toDate, pageable.getPageNumber(), pageable.getPageSize());

        Page<FundEventSummaryDto> page = fundEventRepository.searchEvents(
                fundCode, eventType, status, fromDate, toDate, pageable
        );

        return toPageResponse(page);
    }

	private PageResponse toPageResponse(Page<FundEventSummaryDto> page) {
		PageResponse resp = new PageResponse();
		resp.setContent(page.getContent());
		resp.setTotalElements(page.getTotalElements());
		resp.setTotalPages(page.getTotalPages());
		resp.setNumber(page.getNumber());
		resp.setSize(page.getSize());
		resp.setFirst(page.isFirst());
		resp.setLast(page.isLast());
		return resp;
	}

}

