package com.stellantis.event.controller;


import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.stellantis.event.dto.FundEventDetailDto;
import com.stellantis.event.dto.PageResponse;
import com.stellantis.event.service.FundEventQueryService;
import com.stellantis.event.service.FundEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.stellantis.event.dto.FileDownloadResponse;
import com.stellantis.event.service.EventFileDownloadService;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.media.Content;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FundEventController {

    private final FundEventService fundEventService;
    private final FundEventQueryService fundEventQueryService;
	private final EventFileDownloadService downloadService;


    @Operation(
            summary = "List fund events",
            description = "Returns a paginated list of events for the specified fund, "
                        + "with filters for type, status, and date range.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response"),
                    @ApiResponse(responseCode = "404", description = "Fund not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid filters or pagination input")
            }
    )
    @GetMapping("/funds/{fundCode}/events")
    public PageResponse getEvents(
            @Parameter(description = "Fund business code", required = true)
            @PathVariable String fundCode,

            @Parameter(description = "Filter by event type", schema = @Schema(allowableValues =
                    {"REPLENISHMENT", "MONTHLY_DAILY_BM", "MONTHLY_MONTHLY_BM", "DAILY"}))
            @RequestParam(required = false) String eventType,

            @Parameter(description = "Filter by status", schema = @Schema(allowableValues =
                    {"IN_PROGRESS", "FINISHED", "VALIDATED", "CONFIRMED", "CANCELLED"}))
            @RequestParam(required = false) String status,

            @Parameter(description = "Start of date range filter (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDateFrom,

            @Parameter(description = "End of date range filter (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDateTo,
            @PageableDefault(size = 20)
            Pageable pageable

    ) {
        log.info("Incoming request GET /api/v1/funds/{}/events eventType={} status={} from={} to={} page={} size={}",
                fundCode, eventType, status, startDateFrom, startDateTo,
                pageable.getPageNumber(), pageable.getPageSize());


        return fundEventService.getEvents(
                fundCode,
                eventType,
                status,
                startDateFrom,
                startDateTo,
                pageable
        );
    }
    
	@GetMapping("/funds/{fundCode}/events/{eventId}")
	public ResponseEntity<FundEventDetailDto> getEventDetails(
	        @PathVariable String fundCode,
	        @PathVariable String eventId) {
	    FundEventDetailDto dto = fundEventQueryService
	            .getEventDetails(fundCode, UUID.fromString(eventId));
	    return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Download a single output file (binary stream)")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Binary file content", content = {
			@Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary")),
			@Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary")),
			@Content(mediaType = "text/plain", schema = @Schema(type = "string", format = "binary")) }),
			@ApiResponse(responseCode = "404", description = "File not found"),
			@ApiResponse(responseCode = "503", description = "File unavailable") })
	@GetMapping(value = "/funds/{fundCode}/events/{eventId}/files/{fileId}/download", produces = {
			MediaType.APPLICATION_OCTET_STREAM_VALUE, // fallback binary
			"text/csv", "text/plain" })
	public ResponseEntity<Resource> downloadSingleFile(@PathVariable String fundCode, @PathVariable UUID eventId,
			@PathVariable UUID fileId) {

		FileDownloadResponse file = downloadService.downloadSingleFile(fundCode, eventId, fileId);

		String contentType = (file.getMimeType() == null || file.getMimeType().isBlank())
				? MediaType.APPLICATION_OCTET_STREAM_VALUE
				: file.getMimeType();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentDisposition(ContentDisposition.attachment().filename(file.getFileName()).build());

		ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok().headers(headers)
				.contentType(MediaType.parseMediaType(contentType));

		long actualSize = -1;
		try {
			actualSize = file.getResource().contentLength(); // actual physical file size
		} catch (IOException ignored) {
		}
		if (actualSize > 0) {
			responseBuilder.contentLength(actualSize);
		}
		return responseBuilder.body(file.getResource());

	}
	@GetMapping("/funds/{fundCode}/events/{eventId}/files/download-all")
	public void downloadAllFilesAsZip(@PathVariable String fundCode, @PathVariable UUID eventId,
			HttpServletResponse response) throws IOException {

		String zipName = "events_"+eventId+"_"+fundCode+".zip";

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");

		downloadService.downloadAllFilesAsZip(fundCode, eventId, response.getOutputStream());
	}
}
