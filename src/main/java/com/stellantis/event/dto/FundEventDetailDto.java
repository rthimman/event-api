package com.stellantis.event.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundEventDetailDto {

	private UUID eventId;
	private String eventType;
	private String status;
	private List<String> actions;
	private List<FileEntryDto> reports;
	private List<FileEntryDto> outputFiles;
	
}