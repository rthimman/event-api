package com.stellantis.event.dto;

import org.springframework.core.io.Resource;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class FileDownloadResponse {
	private final Resource resource;
	private final String fileName;
	private final String mimeType;
	private final long sizeBytes;
}