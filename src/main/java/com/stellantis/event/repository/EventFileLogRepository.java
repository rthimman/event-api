package com.stellantis.event.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.stellantis.event.entity.EventFileLogEntity;

public interface EventFileLogRepository extends JpaRepository<EventFileLogEntity, UUID> {

 List<EventFileLogEntity> findByFundEventEntity_IdAndCategoryOrderByCreatedAtAsc(
         UUID eventId, String category);


     @Query(value = """
         SELECT efl.*
         FROM t_event_file_log efl
         JOIN t_fund_event fe ON fe.id_event = efl.id_event
         JOIN t_fund f ON f.id_fund = fe.id_fund
         WHERE efl.id_file = :fileId
           AND efl.id_event = :eventId
           AND f.fund_code = :fundCode
         """,
         nativeQuery = true)
     Optional<EventFileLogEntity> findForDownload(
             @Param("fundCode") String fundCode,
             @Param("eventId") UUID eventId,
             @Param("fileId") UUID fileId);

    @Query(value = """
        SELECT efl.*
        FROM T_EVENT_FILE_LOG efl
        WHERE efl.ID_EVENT = :eventId
        ORDER BY efl.CATEGORY, efl.FILE_NAME
    """, nativeQuery = true)
    List<EventFileLogEntity> findAllForZip(@Param("eventId") UUID eventId);
    
}
 



