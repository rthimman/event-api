package com.stellantis.event.service;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.stellantis.event.exception.FileUnavailableException;

import lombok.RequiredArgsConstructor;

@Service
@Primary 
@RequiredArgsConstructor
public class DelegatingStorageService implements StorageService {

	private final LocalStorageService localStorageService;

	@Nullable
	private final S3StorageService s3StorageService;

	@Override
	public Resource loadAsResource(String storagePath) {

		if (storagePath == null || storagePath.isBlank()) {
			throw new FileUnavailableException("storagePath is null or blank");
		}

		// Route to S3 when path starts with "s3://"
		if (storagePath.startsWith("s3://")) {

			if (s3StorageService == null) {
				// S3 is disabled but an S3 path was provided
				throw new FileUnavailableException("S3 storage is disabled but S3 URI provided: " + storagePath);
			}

			return s3StorageService.loadAsResource(storagePath);
		}

		// Otherwise, use Local filesystem
		return localStorageService.loadAsResource(storagePath);
	}
}