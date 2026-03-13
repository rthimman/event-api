package com.stellantis.event.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stellantis.event.entity.FundEventEntity;

import jakarta.persistence.LockModeType;

public interface FundEventRepository extends JpaRepository<FundEventEntity, UUID>, FundEventRepositoryCustom  {


    @Query("""
        SELECT COUNT(DISTINCT fe.id)
        FROM FundEventEntity fe
        WHERE fe.fundEntity.fundCode = :fundCode
          AND (:eventType IS NULL OR fe.eventType = :eventType)
          AND (:status IS NULL OR fe.status = :status)
          AND (:dateFrom IS NULL OR fe.processingStart >= :dateFrom)
          AND (:dateTo IS NULL OR fe.processingStart <= :dateTo)
    """)
    long countByFilters(
            @Param("fundCode") String fundCode,
            @Param("eventType") String eventType,
            @Param("status") String status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );


        @Query(
            value = """
                SELECT fe.*
                FROM t_fund_event fe
                JOIN t_fund f ON f.id_fund = fe.id_fund
                WHERE fe.id_event = :eventId
                  AND f.fund_code = :fundCode
                """,
            nativeQuery = true
        )
        Optional<FundEventEntity> findByIdAndFundCode(
                @Param("eventId") UUID eventId,
                @Param("fundCode") String fundCode);


        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
            select e from FundEventEntity e
            join fetch e.fundEntity f
            where e.id = :eventId and f.fundCode = :fundCode
        """)
        Optional<FundEventEntity> findForUpdate(@Param("fundCode") String fundCode,
                                                @Param("eventId") UUID eventId);


        @Query(value = """
            SELECT fe.*
            FROM   T_FUND_EVENT fe
            JOIN   T_FUND f ON f.ID_FUND = fe.ID_FUND
            WHERE  fe.ID_EVENT  = :eventId
              AND  f.FUND_CODE  = :fundCode
            """, nativeQuery = true)
        Optional<FundEventEntity> findEntityByIdAndFundCode(@Param("eventId") UUID eventId,
                                                      @Param("fundCode") String fundCode);




}
