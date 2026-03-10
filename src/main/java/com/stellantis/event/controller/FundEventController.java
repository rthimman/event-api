package com.stellantis.event.controller;


import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stellantis.event.dto.PageResponse;
import com.stellantis.event.service.FundEventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FundEventController {

    private final FundEventService fundEventService;

    @Operation(
            summary = "List fund events",
            description = "Returns a paginated list of events for the specified fund, "
                        + "with filters for type, status, and date range.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response"),
                    @ApiResponse(responseCode = "404", description = "Fund not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid filters or pagination input")
            }
    )
    @GetMapping("/funds/{fundCode}/events")
    public PageResponse getEvents(
            @Parameter(description = "Fund business code", required = true)
            @PathVariable String fundCode,

            @Parameter(description = "Filter by event type", schema = @Schema(allowableValues =
                    {"REPLENISHMENT", "MONTHLY_DAILY_BM", "MONTHLY_MONTHLY_BM", "DAILY"}))
            @RequestParam(required = false) String eventType,

            @Parameter(description = "Filter by status", schema = @Schema(allowableValues =
                    {"IN_PROGRESS", "FINISHED", "VALIDATED", "CONFIRMED", "CANCELLED"}))
            @RequestParam(required = false) String status,

            @Parameter(description = "Start of date range filter (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDateFrom,

            @Parameter(description = "End of date range filter (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDateTo,
            @PageableDefault(size = 20)
            Pageable pageable

    ) {
        log.info("Incoming request GET /api/v1/funds/{}/events eventType={} status={} from={} to={} page={} size={}",
                fundCode, eventType, status, startDateFrom, startDateTo,
                pageable.getPageNumber(), pageable.getPageSize());


        return fundEventService.getEvents(
                fundCode,
                eventType,
                status,
                startDateFrom,
                startDateTo,
                pageable
        );
    }

}
