package com.stellantis.event.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stellantis.event.dto.FileEntryDto;
import com.stellantis.event.dto.FundEventDetailDto;
import com.stellantis.event.entity.EventFileLogEntity;
import com.stellantis.event.entity.FundEventEntity;
import com.stellantis.event.exception.EventNotFoundException;
import com.stellantis.event.exception.FundNotFoundException;
import com.stellantis.event.repository.EventFileLogRepository;
import com.stellantis.event.repository.FundEventRepository;
import com.stellantis.event.repository.FundRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FundEventQueryService {
	
	 private final FundRepository fundRepository;
	 private final FundEventRepository fundEventRepository;
	 private final EventFileLogRepository eventFileLogRepository;

 @Transactional(readOnly = true)
 public FundEventDetailDto getEventDetails(String fundCode, UUID eventId) {

	 if (!fundRepository.existsByFundCode(fundCode)) {
		    throw new FundNotFoundException(fundCode);
		}

		FundEventEntity event = fundEventRepository
		        .findByIdAndFundCode(eventId, fundCode)
		        .orElseThrow(() -> new EventNotFoundException(eventId.toString()));
		
     List<EventFileLogEntity> reportFiles =
             eventFileLogRepository.findByFundEventEntity_IdAndCategoryOrderByCreatedAtAsc(
                     eventId, "REPORT");

     List<EventFileLogEntity> outputFiles =
             eventFileLogRepository.findByFundEventEntity_IdAndCategoryOrderByCreatedAtAsc(
                     eventId, "OUTPUT_FILE");

     return FundEventDetailDto.builder()
             .eventId(event.getId())
             .eventType(event.getEventType() != null ? event.getEventType().name() : null)
             .status(event.getStatus().name())
             .actions(computeActions(event.getStatus()))
             .reports(reportFiles.stream().map(this::toFileEntry).collect(Collectors.toList()))
             .outputFiles(outputFiles.stream().map(this::toFileEntry).collect(Collectors.toList()))
             .build();
 }

 private FileEntryDto toFileEntry(EventFileLogEntity efl) {
     return FileEntryDto.builder()
             .fileId(efl.getId())
             .fileName(efl.getFileName())
             .fileType(efl.getFileType())
             .sizeBytes(efl.getSizeBytes())
             .isTiersRestricted(efl.getTiersRestricted())
             .build();
 }
 public static String toString(LocalDateTime dateTime) {
	    return dateTime != null ? dateTime.toString() : null;
	}

 private List<String> computeActions(FundEventEntity.EventStatus status) {
     if (status == FundEventEntity.EventStatus.VALIDATED) {
         return List.of("CONFIRM", "CANCEL_VALIDATION");
     }
     return List.of();
 }
}