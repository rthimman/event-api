package com.stellantis.event.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundEventSummaryDto {

	private UUID eventId;
	private String fundCode;
	private String eventType;
	private String simulationId;
	private String status;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private BigDecimal opbPerforming;
	private Boolean isInitialTransfer;
	private Integer reportCount;
	private Integer outputFileCount;
	private String createdBy;
	private List<String> actions;
	private List<FileEntryDto> reports;
 	private List<FileEntryDto> outputFiles;

}
