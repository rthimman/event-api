package com.stellantis.event.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stellantis.event.entity.FundEventEntity;
import com.stellantis.event.exception.ApiException;
import com.stellantis.event.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportServiceImpl implements ExportService {

    @Value("${app.export.dir:./export}")
    private String exportDir;

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Generate the REFTIT file for the event as part of CONFIRM. Must succeed together with accounting.  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     */
    @Override
    public void generateReftit(FundEventEntity event) {
        try {
            Path base = Paths.get(exportDir).toAbsolutePath().normalize();
            Files.createDirectories(base);

            String fileName = "REFTIT_" + event.getFundEntity().getFundCode()
                    + "_" + event.getId()
                    + "_" + (event.getProcessingStart() != null ? TS.format(event.getProcessingStart()) : "NA")
                    + ".txt";

            Path file = base.resolve(fileName);
            log.info("[Export] Writing REFTIT file {}", file);

            // Example content — replace with your actual fixed-width export format
            try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                bw.write("REFTIT HEADER | fund=" + event.getFundEntity().getFundCode()
                        + " | event=" + event.getId());
                bw.newLine();
                bw.write("DETAILS GO HERE ...");
                bw.newLine();
                bw.write("REFTIT FOOTER");
                bw.newLine();
            }
        } catch (IOException ioe) {
            log.error("[Export] REFTIT generation failed for event {}", event.getId(), ioe);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "REFTIT generation failed for event " + event.getId(), ioe);
        }
    }
}
