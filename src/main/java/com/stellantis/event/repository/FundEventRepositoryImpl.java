package com.stellantis.event.repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.stellantis.event.dto.FileEntryDto;
import com.stellantis.event.dto.FundEventSummaryDto;
import com.stellantis.event.util.ActionResolver;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class FundEventRepositoryImpl implements FundEventRepositoryCustom {

	@PersistenceContext
	private EntityManager em;


	private static final Map<String, String> SORT_COLUMN_MAP = Map.ofEntries(
	    // Event fields
	   // Map.entry("eventDate",        "e.EVENT_DATE"),
	    Map.entry("processingStart",  "e.PROCESSING_START"),
	    Map.entry("startDate",        "e.PROCESSING_START"),
	   // Map.entry("endDate",          "e.PROCESSING_END"),
	    Map.entry("eventType",        "e.EVENT_TYPE"),
	    Map.entry("status",           "e.STATUS"),
	    Map.entry("fundCode",         "f.FUND_CODE")
	  
//	    Map.entry("fundName",         "f.DESCRIPTION"),
//	    Map.entry("fileType",         "fl.FILE_TYPE"),
//	    Map.entry("category",         "fl.CATEGORY"),
//	    Map.entry("reportType",       "fl.REPORT_TYPE"),
//	    Map.entry("sizeBytes",        "fl.SIZE_BYTES")
	);

	@Override
	public Page<FundEventSummaryDto> searchEvents(
	        String countryCode,
	        String fundCode,
	        String eventType,
	        String status,
	        LocalDateTime startDateFrom,
	        LocalDateTime startDateTo,
	        Pageable pageable) {

	    StringBuilder sql = new StringBuilder("""
	        SELECT
	            f.ID_FUND,
	            f.FUND_CODE,
	            f.DESCRIPTION AS FUND_NAME,
	            e.ID_EVENT,
	            e.EVENT_DISPLAY_ID,
	            e.EVENT_TYPE,
	            e.EVENT_DATE,
	            e.STATUS,
	            e.PROCESSING_START,
	            e.PROCESSING_END,
	            e.OPB_PERFORMING,
	            fl.ID_FILE,
	            fl.FILE_NAME,
	            fl.FILE_TYPE,
	            fl.CATEGORY,
	            fl.REPORT_TYPE,
	            fl.SIZE_BYTES,
	            fl.IS_TIERS_RESTRICTED,
	            fl.CREATED_AT AS FILE_CREATED_AT
	        FROM T_FUND f
	        INNER JOIN T_FUND_EVENT e ON e.ID_FUND = f.ID_FUND
	        LEFT JOIN T_EVENT_FILE_LOG fl ON fl.ID_EVENT = e.ID_EVENT
	        WHERE f.COUNTRY_CODE = :countryCode
	          AND f.FUND_CODE = :fundCode
	    """);

	    StringBuilder countSql = new StringBuilder("""
	        SELECT COUNT(DISTINCT e.ID_EVENT)
	        FROM T_FUND f
	        INNER JOIN T_FUND_EVENT e ON e.ID_FUND = f.ID_FUND
	        WHERE f.COUNTRY_CODE = :countryCode
	          AND f.FUND_CODE = :fundCode
	    """);

	    // ---------------- Filters ----------------
	    Map<String, Object> params = new HashMap<>();
	    params.put("countryCode", countryCode);
	    params.put("fundCode", fundCode);

	    if (StringUtils.hasText(eventType)) {
	        sql.append(" AND e.EVENT_TYPE = :eventType");
	        countSql.append(" AND e.EVENT_TYPE = :eventType");
	        params.put("eventType", eventType);
	    }

	    if (StringUtils.hasText(status)) {
	        sql.append(" AND e.STATUS = :status");
	        countSql.append(" AND e.STATUS = :status");
	        params.put("status", status);
	    }

	    if (startDateFrom != null) {
	        sql.append(" AND e.EVENT_DATE >= :startDateFrom");
	        countSql.append(" AND e.EVENT_DATE >= :startDateFrom");
	        params.put("startDateFrom", startDateFrom);
	    }

	    if (startDateTo != null) {
	        sql.append(" AND e.EVENT_DATE <= :startDateTo");
	        countSql.append(" AND e.EVENT_DATE <= :startDateTo");
	        params.put("startDateTo", startDateTo);
	    }

	    // ---------------- Sorting ----------------
	    String orderBy = buildOrderBy(pageable);
	    if (orderBy.isEmpty()) {
	        orderBy = """
	            ORDER BY e.EVENT_DATE DESC,
	                     e.PROCESSING_START DESC,
	                     fl.CATEGORY,
	                     fl.FILE_TYPE
	        """;
	    }
	    sql.append(orderBy);

	    // ---------------- Execute Queries ----------------
	    Query dataQ = em.createNativeQuery(sql.toString());
	    Query countQ = em.createNativeQuery(countSql.toString());

	    params.forEach((k, v) -> {
	        dataQ.setParameter(k, v);
	        countQ.setParameter(k, v);
	    });

	    dataQ.setFirstResult((int) pageable.getOffset());
	    dataQ.setMaxResults(pageable.getPageSize());

	    List<Object[]> rows = dataQ.getResultList();
	    List<FundEventSummaryDto> content = mapRows(rows);

	    Long totalElements = ((Number) countQ.getSingleResult()).longValue();
	    return new PageImpl<>(content, pageable, totalElements);
	}


	private String buildOrderBy(Pageable pageable) {
	    if (pageable == null || pageable.getSort() == null || pageable.getSort().isUnsorted()) {
	        return "";
	    }

	    List<String> orderParts = new ArrayList<>();

	    pageable.getSort().forEach(order -> {
	        String property = order.getProperty();     // e.g., "startDate"
	        String column = SORT_COLUMN_MAP.get(property);

	        // Allow only whitelisted fields
	        if (column != null) {
	            String direction = order.isAscending() ? "ASC" : "DESC";

	            // Optional: NULLS LAST only for date fields
	            boolean applyNullsLast = column.equals("e.PROCESSING_START");

	            if (applyNullsLast) {
	                orderParts.add(column + " " + direction + " NULLS LAST");
	            } else {
	                orderParts.add(column + " " + direction);
	            }
	        }
	    });

	    if (orderParts.isEmpty()) {
	        return "";
	    }

	    return " ORDER BY " + String.join(", ", orderParts);
	}
	private List<FundEventSummaryDto> mapRows(List<Object[]> rows) {

	    Map<UUID, FundEventSummaryDto> map = new LinkedHashMap<>();

	    rows.forEach(r -> {
	        UUID eventId = (UUID) r[3];

	        // Create event only if absent
	        map.computeIfAbsent(eventId, id -> new FundEventSummaryDto(
	                id,
	                (String) r[1],             // fundCode
	                (String) r[5],             // eventType
	                (String) r[4],             // eventDisplayId
	                (String) r[7],             // status
	                toLocalDateTime(r[8]),     // startDate
	                toLocalDateTime(r[9]),     // endDate
	                toBigDecimal(r[10]),       // opbPerforming
	                false,                     // isInitialTransfer
	                0,                         // reportCount
	                0,                         // outputFileCount
	                "system",                  // createdBy
	                ActionResolver.resolve((String) r[7], toLocalDateTime(r[8]).toLocalDate()),                      // actions
	                new ArrayList<>(),         // reports
	                new ArrayList<>()          // output files
	        ));

	        // File mapping — if a file exists
	        UUID fileId = (UUID) r[11];
	        if (fileId != null) {
	            FileEntryDto file = new FileEntryDto(
	                    fileId,
	                    (String) r[12],
	                    (String) r[13],
	                    (String) r[14],
	                    (String) r[15],
	                    toLong(r[16]),
	                    toBoolean(r[17]),
	                    r[18] != null ? r[18].toString() : null
	            );

	            String category = (String) r[14];

	            if ("REPORT".equals(category)) {
	                map.get(eventId).getReports().add(file);
	            } else if ("OUTPUT_FILE".equals(category)) {
	                map.get(eventId).getOutputFiles().add(file);
	            }
	        }
	    });

	    return new ArrayList<>(map.values());
	}
	private LocalDateTime toLocalDateTime(Object o) {
		return (o instanceof Timestamp ts) ? ts.toLocalDateTime() : null;
	}

	private Long toLong(Object o) {
		return (o instanceof Number n) ? n.longValue() : 0L;
	}

	private boolean toBoolean(Object o) {
		if (o instanceof Boolean b)
			return b;
		if (o instanceof Number n)
			return n.intValue() != 0;
		if (o instanceof String s)
			return "Y".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s);
		return false;
	}

//    private int toInt(Object o) {
//        if (o == null) return 0;
//        if (o instanceof BigInteger bi) return bi.intValue();
//        if (o instanceof Number n) return n.intValue();
//        return Integer.parseInt(o.toString());
//    }

	private BigDecimal toBigDecimal(Object o) {
		if (o == null)
			return null;
		if (o instanceof BigDecimal bd)
			return bd;
		if (o instanceof BigInteger bi)
			return new BigDecimal(bi);
		if (o instanceof Number n) {
			// For types like Integer, Long, Double, Float
			return BigDecimal.valueOf(n.doubleValue());
		}

		if (o instanceof String s) {
			s = s.trim();
			if (s.isEmpty())
				return null;
			try {
				return new BigDecimal(s);
			} catch (NumberFormatException e) {
				return null; // or throw an exception if you prefer strict behavior
			}
		}

		return null;
	}

}
