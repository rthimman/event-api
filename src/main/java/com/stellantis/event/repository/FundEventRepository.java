package com.stellantis.event.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stellantis.event.entity.FundEventEntity;

public interface FundEventRepository extends JpaRepository<FundEventEntity, UUID> {

	@Query(value = """
			SELECT fe.ID_EVENT,
			       fe.FUND_CODE,
			       fe.EVENT_TYPE,
			       fe.SIMULATION_ID,
			       fe.STATUS,
			       fe.START_DATE,
			       fe.END_DATE,
			       fe.OPB_PERFORMING,
			       fe.IS_INITIAL_TRANSFER,
			       fe.CREATED_BY,
			       COUNT(CASE WHEN efl.CATEGORY='REPORT' THEN 1 END) AS REPORT_COUNT,
			       COUNT(CASE WHEN efl.CATEGORY='OUTPUT_FILE' THEN 1 END) AS OUTPUT_FILE_COUNT
			FROM T_FUND_EVENT fe
			LEFT JOIN T_EVENT_FILE_LOG efl
			    ON fe.ID_EVENT = efl.ID_EVENT
			WHERE fe.FUND_CODE = :fundCode
			  AND (:eventType IS NULL OR fe.EVENT_TYPE = :eventType)
			  AND (:status IS NULL OR fe.STATUS = :status)
			  AND (:fromDate IS NULL OR fe.START_DATE >= :fromDate)
			  AND (:toDate IS NULL OR fe.START_DATE <= :toDate)
			GROUP BY fe.ID_EVENT, fe.FUND_CODE, fe.EVENT_TYPE,
			         fe.SIMULATION_ID, fe.STATUS, fe.START_DATE,
			         fe.END_DATE, fe.OPB_PERFORMING,
			         fe.IS_INITIAL_TRANSFER, fe.CREATED_BY
			""", countQuery = """
			        SELECT count(*) FROM T_FUND_EVENT fe
			        WHERE fe.FUND_CODE = :fundCode
			          AND (:eventType IS NULL OR fe.EVENT_TYPE = :eventType)
			          AND (:status IS NULL OR fe.STATUS = :status)
			          AND (:fromDate IS NULL OR fe.START_DATE >= :fromDate)
			          AND (:toDate IS NULL OR fe.START_DATE <= :toDate)
			""", nativeQuery = true)
	Page<Object[]> searchEvents(@Param("fundCode") String fundCode, @Param("eventType") String eventType,
			@Param("status") String status, @Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate, Pageable pageable);
	

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


}
