package com.stellantis.event.service;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stellantis.event.entity.FundEventEntity;
import com.stellantis.event.exception.ApiException;
import com.stellantis.event.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountingServiceImpl implements AccountingService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Generate accounting entries for the event as part of CONFIRM.
     * If this fails, the whole CONFIRM must fail (atomic with REFTIT).  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     */
    @Override
    @Transactional
    public void generateAccounting(FundEventEntity event) {
        log.info("[Accounting] Generating entries for event {}", event.getId());

        try {
            // Example insert — replace columns with your real schema of T_FUND_ACCOUNTING
            String sql = """
                INSERT INTO T_FUND_ACCOUNTING (ID_ACCOUNTING, ID_EVENT, CREATED_AT, CREATED_BY)
                VALUES (?, ?, now(), ?)
                """;
            int affected = jdbcTemplate.update(sql,
                    UUID.randomUUID(),
                    event.getId(),
                    event.getCreatedBy());

            if (affected != 1) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "No accounting row inserted for event " + event.getId());
            }
        } catch (Exception ex) {
            log.error("[Accounting] Failed for event {}", event.getId(), ex);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Accounting generation failed for event " + event.getId(), ex);
        }
    }
}

