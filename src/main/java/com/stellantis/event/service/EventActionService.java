package com.stellantis.event.service;


import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stellantis.event.converter.EventActionConverter;
import com.stellantis.event.dto.ActionType;
import com.stellantis.event.dto.EventActionResultDto;
import com.stellantis.event.dto.EventStatus;
import com.stellantis.event.entity.FundEntity;
import com.stellantis.event.entity.FundEventEntity;
import com.stellantis.event.exception.ApiException;
import com.stellantis.event.exception.ErrorCode;
import com.stellantis.event.repository.FundEventRepository;
import com.stellantis.event.repository.FundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventActionService {

    private final FundRepository fundRepository;
    private final FundEventRepository eventRepository;
    private final AccountingService accountingService;
    private final ExportService exportService;
    private final ContractService contractService;
    private final PartnerService partnerService;
    private final EventActionConverter converter;

    /**
     * Execute a lifecycle action per E-05 rules:
     * - VALIDATE: only on FINISHED and only on day J (server date == PROCESSING_START date)  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     * - CONFIRM: only on VALIDATED; generates accounting + REFTIT atomically  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     * - CANCEL_VALIDATION: only on VALIDATED → FINISHED  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     * - FREE_CONTRACTS: only on FINISHED → CANCELLED and releases contracts  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     * - SEND_TO_PARTNER: only on CONFIRMED; idempotent using SENT_TO_PARTNER_AT  [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
     */
    @Transactional
    public EventActionResultDto executeAction(String fundCode,
                                              UUID eventId,
                                              ActionType action,
                                              String currentUser) {

        // Fund existence and event fetch (with PESSIMISTIC_WRITE lock in repository)
        FundEntity fund = fundRepository.findByFundCode(fundCode)
                .orElseThrow(() -> new ApiException(ErrorCode.FUND_NOT_FOUND, "Fund not found: " + fundCode));  // [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)

        FundEventEntity event = eventRepository.findForUpdate(fundCode, eventId)
                .orElseThrow(() -> new ApiException(ErrorCode.EVENT_NOT_FOUND, "Event not found: " + eventId));  // [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)

        String previousStatus = event.getStatus().name();
        String newStatus = previousStatus;
        String message = null;

        log.info("Executing action {} on event {} (fund={}) by {}. Current status={}",
                action, eventId, fundCode, currentUser, previousStatus);

        switch (action) {

            case VALIDATE -> {
                // VALIDATE requires FINISHED and must be executed on day J (PROCESSING_START date == today)  
                ensureStatus(previousStatus, EventStatus.FINISHED.name(), action);  
                if (!isDayJ(event)) {
                    throw new ApiException(ErrorCode.DAY_J_REQUIRED, "VALIDATE only allowed on event day");  // [1](https://capgemini-my.sharepoint.com/personal/rajesh_thimmani_capgemini_com/_layouts/15/Doc.aspx?sourcedoc=%7BAE3DD411-38B9-4810-B0E6-3E51ACD9E140%7D&file=E05_Execute_Event_Action_corrected.docx&action=default&mobileredirect=true)
                }
                newStatus = EventStatus.VALIDATED.name();
               // event.setStatus(newStatus); -- TODO: need to map proper data
            }

            case CONFIRM -> {
                // CONFIRM requires VALIDATED; must generate accounting + REFTIT atomically  
                ensureStatus(previousStatus, EventStatus.VALIDATED.name(), action);  
                accountingService.generateAccounting(event);   
                exportService.generateReftit(event);          
                newStatus = EventStatus.CONFIRMED.name();
                //event.setStatus(newStatus); -- TODO: need to map proper data
            }

            case CANCEL_VALIDATION -> {
                // CANCEL_VALIDATION allowed only on VALIDATED → FINISHED  
                ensureStatus(previousStatus, EventStatus.VALIDATED.name(), action);  
                newStatus = EventStatus.FINISHED.name();
               // event.setStatus(newStatus); -- TODO: need to map proper data
            }

            case FREE_CONTRACTS -> {
                // FREE_CONTRACTS allowed only on FINISHED → CANCELLED; must release locked contracts  
                ensureStatus(previousStatus, EventStatus.FINISHED.name(), action);  
                contractService.releaseContractsForEvent(event.getId());            
                newStatus = EventStatus.CANCELLED.name();
                //event.setStatus(newStatus);-- TODO: need to map proper data
            }

            case SEND_TO_PARTNER -> {
                // SEND_TO_PARTNER allowed only on CONFIRMED; one-time only (idempotent)  
                ensureStatus(previousStatus, EventStatus.CONFIRMED.name(), action); 
                if (event.getSentToPartnerAt() != null) {
                    throw new ApiException(ErrorCode.ALREADY_SENT_TO_PARTNER, "Event already sent to partner");  
                }
                message = partnerService.sendToPartner(event);                      
                //event.setSentToPartnerAt(OffsetDateTime.now());                     
                newStatus = EventStatus.CONFIRMED.name();                           
            }

            default -> throw new ApiException(ErrorCode.INVALID_ACTION, "Unsupported action: " + action); 
        }

        eventRepository.save(event);

        return converter.toResult(
                event,
                action.name(),
                previousStatus,
                newStatus,
                currentUser,
                OffsetDateTime.now(),
                message
        );
    }

    // Ensure the event is in the required status before applying the action.  [
    private void ensureStatus(String actual, String required, ActionType action) {
        if (!required.equalsIgnoreCase(actual)) {
            throw new ApiException(
                    ErrorCode.ACTION_NOT_ALLOWED,
                    "Action " + action + " not allowed on status " + actual
            ); 
        }
    }

    // Day-J check: VALIDATE allowed only if event's PROCESSING_START date equals today's date.  
    private boolean isDayJ(FundEventEntity event) {
        if (event.getProcessingStart() == null) return false;
        return event.getProcessingStart().toLocalDate().isEqual(LocalDate.now());
    }
}

