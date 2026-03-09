package com.stellantis.event.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stellantis.event.dto.FundEventSummaryDto;
import com.stellantis.event.service.FundEventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FundEventController {

	@Autowired
	private FundEventService fundEventService;
	
	@GetMapping("/funds/{fundCode}/events")
	public ResponseEntity<Page<FundEventSummaryDto>> getEvents(@PathVariable String fundCode,
			@RequestParam(required = false) String eventType, @RequestParam(required = false) String status,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDateTo,
			@PageableDefault(size = 20, sort = "startDate", direction = Direction.DESC) Pageable pageable) {
		Page<FundEventSummaryDto> response = fundEventService.getEvents(fundCode, status, eventType, startDateFrom,
				startDateTo, pageable);
		return ResponseEntity.ok(response);
	}

}
