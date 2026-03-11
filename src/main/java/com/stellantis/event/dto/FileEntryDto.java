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
	private Long sizeBytes;
	private Boolean isTiersRestricted;
}
