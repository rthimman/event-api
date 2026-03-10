package com.stellantis.event.dto;

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
	//private String reportType;
	private Long sizeBytes;
	//private String mimeType;
	private Boolean isTiersRestricted;
	//private String createdAt;
}
