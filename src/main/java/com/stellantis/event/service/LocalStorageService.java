package com.stellantis.event.service;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.stellantis.event.exception.FileUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            Path path = resolvePath(storagePath);
            File file = path.toFile();

            if (!file.exists() || !file.isFile()) {
                throw new FileUnavailableException("Local file not found: " + path);
            }

            return new FileSystemResource(file);
        } catch (IllegalArgumentException iae) {
            throw new FileUnavailableException("Invalid local path: " + storagePath, iae);
        } catch (Exception e) {
            throw new FileUnavailableException("Failed to access local file: " + storagePath, e);
        }
    }

    private Path resolvePath(String storagePath) {
        // Allow raw absolute paths or "file://"
        if (storagePath == null || storagePath.isBlank()) {
            throw new IllegalArgumentException("storagePath is blank");
        }
        if (storagePath.startsWith("file://")) {
            return Paths.get(URI.create(storagePath));
        }
        // If the path has a scheme other than file, it is not local
        if (storagePath.contains("://") && !storagePath.startsWith("file://")) {
            throw new IllegalArgumentException("Non-local scheme: " + storagePath);
        }
        return Paths.get(storagePath);
    }
}