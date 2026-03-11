package com.stellantis.event.service;

import org.springframework.core.io.Resource;

public interface StorageService {
	Resource loadAsResource(String storagePath);
}
