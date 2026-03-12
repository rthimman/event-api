package com.stellantis.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntryDto {

	private UUID fileId;
	private String fileName;
	private String fileType;
	private String category;
	private String reportType;
	private Long sizeBytes;
	private Boolean isTiersRestricted;
	private String createdAt;
}
