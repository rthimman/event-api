package com.stellantis.event.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class PartnerServiceImpl implements PartnerService {

    @Value("${app.export.dir:./export}")
    private String exportDir;

    @Value("${app.partner.outbox:./partner_outbox}")
    private String partnerOutbox;

    /**
     * SEND_TO_PARTNER: copy all generated files for the event into partner outbox.  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     * Returns message with how many files were delivered.
     */
    @Override
    public String sendToPartner(FundEventEntity event) {
        try {
            Path source = Paths.get(exportDir).toAbsolutePath().normalize();
            Path target = Paths.get(partnerOutbox).toAbsolutePath().normalize();
            Files.createDirectories(target);

            // Simple heuristic: pick files containing the eventId (e.g., "REFTIT_<fund>_<event>.txt")
            String eventToken = event.getId().toString();

            List<Path> files;
            try (Stream<Path> stream = Files.list(source)) {
                files = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().contains(eventToken))
                        .collect(Collectors.toList());
            }

            for (Path f : files) {
                Path dest = target.resolve(f.getFileName().toString());
                log.info("[Partner] Copying {} -> {}", f, dest);
                Files.copy(f, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            String msg = "Delivered " + files.size() + " file(s) to partner outbox";
            log.info("[Partner] {}", msg);
            return msg;

        } catch (IOException ioe) {
            log.error("[Partner] Delivery failed for event {}", event.getId(), ioe);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Partner delivery failed for event " + event.getId(), ioe);
        }
    }
}

