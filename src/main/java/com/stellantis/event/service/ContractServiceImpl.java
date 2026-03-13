package com.stellantis.event.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stellantis.event.exception.ApiException;
import com.stellantis.event.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Choose release strategy:
     * - CANCEL = mark rows CANCELLED
     * - DELETE = delete rows
     */
    @Value("${app.contracts.release-mode:CANCEL}") // CANCEL | DELETE
    private String releaseMode;

    /**
     * FREE_CONTRACTS: release all locked contracts for the event.  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     */
    @Override
    @Transactional
    public void releaseContractsForEvent(UUID eventId) {
        log.info("[Contracts] Releasing locks for event {} (mode={})", eventId, releaseMode);

        try {
            int affected;
            if ("DELETE".equalsIgnoreCase(releaseMode)) {
                // Example: delete from a lock table by event
                affected = jdbcTemplate.update(
                        "DELETE FROM T_SIMULATION_DETAIL WHERE ID_EVENT = ?",
                        eventId
                );
            } else {
                // Default strategy: mark CANCELLED
                affected = jdbcTemplate.update(
                        "UPDATE T_SIMULATION_DETAIL SET STATUS = 'CANCELLED' WHERE ID_EVENT = ?",
                        eventId
                );
            }
            log.info("[Contracts] Released {} rows for event {}", affected, eventId);
        } catch (Exception ex) {
            log.error("[Contracts] Release failed for event {}", eventId, ex);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Contract release failed for event " + eventId, ex);
        }
    }
}

