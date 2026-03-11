package com.stellantis.event.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.stellantis.event.dto.FileDownloadResponse;
import com.stellantis.event.entity.EventFileLogEntity;
import com.stellantis.event.exception.EventNotFoundException;
import com.stellantis.event.exception.FileNotAllowedException;
import com.stellantis.event.exception.FileNotFoundException;
import com.stellantis.event.exception.FileUnavailableException;
import com.stellantis.event.exception.FundNotFoundException;
import com.stellantis.event.repository.EventFileLogRepository;
import com.stellantis.event.repository.FundEventRepository;
import com.stellantis.event.repository.FundRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventFileDownloadService {

	private final EventFileLogRepository fileLogRepository;
	private final StorageService storageService; // Your abstraction (local/S3/etc.)
	private final FundRepository fundRepository;
	private final FundEventRepository fundEventRepository;

	public FileDownloadResponse downloadSingleFile(String fundCode, UUID eventId, UUID fileId) {

		EventFileLogEntity efl = fileLogRepository.findForDownload(fundCode, eventId, fileId)
				.orElseThrow(() -> new FileNotFoundException("File not found for download"));

		if (!fundRepository.existsByFundCode(fundCode)) {
			throw new FundNotFoundException(fundCode);
		}

		fundEventRepository.findByIdAndFundCode(eventId, fundCode)
				.orElseThrow(() -> new EventNotFoundException(eventId.toString()));
		

//        boolean isAdmin = authenticationFacade.hasRole("ADMIN_IT");
//        if (Boolean.TRUE.equals(efl.getTiersRestricted()) && !isAdmin) {
//        	throw new AccessDeniedException("Access denied: TIERS file requires ADMIN_IT role"); 
//        }
		
		validateFileType(efl);

		Resource resource = storageService.loadAsResource(efl.getStoragePath());

		if (!resource.exists()) {
			throw new FileUnavailableException("File not available at storage backend");
		}

		try {
			return FileDownloadResponse.builder().resource(resource).fileName(efl.getFileName()).mimeType(efl.getMimeType())
					.sizeBytes(efl.getSizeBytes() == null ? resource.contentLength() : efl.getSizeBytes()).build();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	public void downloadAllFilesAsZip(String fundCode, UUID eventId, OutputStream outputStream) {

		List<EventFileLogEntity> files = fileLogRepository.findAllForZip(eventId); 

		// Check TIERS access rule
//	    if (files.isEmpty()) {
//	        throw new EntityNotFoundException("No files found for event: " + eventId);
//	    }
//	    
//	     boolean anyRestrictedFile = files.stream().anyMatch(f -> Boolean.TRUE.equals(f.getTiersRestricted()));
//	     boolean isAdmin = authenticationFacade.hasRole("ADMIN_IT");
//	     if (anyRestrictedFile && !isAdmin) {
//	         throw new AccessDeniedException("ZIP contains TIERS-restricted files. ADMIN_IT role required.");
//	     }
		
		if (!fundRepository.existsByFundCode(fundCode)) {
			throw new FundNotFoundException(fundCode);
		}

		fundEventRepository.findByIdAndFundCode(eventId, fundCode)
				.orElseThrow(() -> new EventNotFoundException(eventId.toString()));


		if (files.isEmpty()) {
			throw new FileNotFoundException("No files available for this event");
		}

		try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {

			for (EventFileLogEntity entry : files) {

				validateFileType(entry);

				Resource resource = storageService.loadAsResource(entry.getStoragePath());

				if (!resource.exists()) {
					throw new FileUnavailableException("File not available: " + entry.getFileName());
				}

				zos.putNextEntry(new ZipEntry(entry.getFileName()));

				try (InputStream is = resource.getInputStream()) {
					is.transferTo(zos);
				}

				zos.closeEntry();
			}

		} catch (IOException e) {
			throw new FileUnavailableException("Failed to stream ZIP", e);
		}
	}

	private void validateFileType(EventFileLogEntity entry) {
		String fileName = entry.getFileName().toLowerCase();
		String mimeType = entry.getMimeType() == null ? "" : entry.getMimeType().toLowerCase();

		boolean isCsv = fileName.endsWith(".csv") || mimeType.contains("csv");
		boolean isTxt = fileName.endsWith(".txt") || mimeType.contains("text/plain");

		if (!(isCsv || isTxt)) {
			throw new FileNotAllowedException("Only .csv and .txt files are allowed. File: " + entry.getFileName());
		}
	}
}
