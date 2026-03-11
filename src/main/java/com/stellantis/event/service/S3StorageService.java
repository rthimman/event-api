package com.stellantis.event.service;

import java.io.InputStream;
import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.stellantis.event.exception.FileUnavailableException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "storage.s3", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final AmazonS3 amazonS3;

    @Override
    public Resource loadAsResource(String storagePath) {
        if (!isS3Uri(storagePath)) {
            throw new FileUnavailableException("Not an S3 URI: " + storagePath);
        }
        S3Location loc = parseS3Uri(storagePath);

        try {
            if (!amazonS3.doesObjectExist(loc.bucket(), loc.key())) {
                throw new FileUnavailableException("S3 object not found: s3://" + loc.bucket() + "/" + loc.key());
            }

            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(loc.bucket(), loc.key()));
            long contentLength = s3Object.getObjectMetadata().getContentLength();
            InputStream is = s3Object.getObjectContent();

            return new LengthAwareInputStreamResource(is, contentLength);
        } catch (FileUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new FileUnavailableException("Failed to load S3 object: " + storagePath, e);
        }
    }

    private boolean isS3Uri(String path) {
        return path != null && path.startsWith("s3://");
    }

    private S3Location parseS3Uri(String uri) {
        try {
            URI u = URI.create(uri);
            String bucket = u.getHost();
            String key = u.getPath();
            if (key.startsWith("/")) key = key.substring(1);

            if (bucket == null || bucket.isBlank() || key.isBlank()) {
                throw new IllegalArgumentException("Invalid S3 URI: " + uri);
            }
            return new S3Location(bucket, key);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid S3 URI: " + uri, ex);
        }
    }

    private record S3Location(String bucket, String key) {}

    static class LengthAwareInputStreamResource extends InputStreamResource {
        private final long length;

        public LengthAwareInputStreamResource(InputStream inputStream, long length) {
            super(inputStream);
            this.length = length;
        }

        @Override
        public long contentLength() {
            return length >= 0 ? length : -1;
        }

        @Override
        @Nullable
        public String getFilename() {
            // Not required; the controller uses DB fileName for Content-Disposition
            return null;
        }
    }
}