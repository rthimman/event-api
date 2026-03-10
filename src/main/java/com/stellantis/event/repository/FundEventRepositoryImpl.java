package com.stellantis.event.repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.stellantis.event.dto.FundEventSummaryDto;
import com.stellantis.event.util.ActionResolver;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class FundEventRepositoryImpl implements FundEventRepositoryCustom {


    @PersistenceContext
    private EntityManager em;

    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "processingStart", "fe.PROCESSING_START",
            "startDate",       "fe.PROCESSING_START", // alias supported for convenience
            "eventType",       "fe.EVENT_TYPE",
            "status",          "fe.STATUS",
            "fundCode",        "f.FUND_CODE"
    );

    @Override
    public Page<FundEventSummaryDto> searchEvents(
            String fundCode,
            String eventType,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        StringBuilder sql = new StringBuilder();
        StringBuilder count = new StringBuilder();

        sql.append("""
            SELECT 
              fe.ID_EVENT,
              f.FUND_CODE,
              fe.EVENT_TYPE,
              fe.EVENT_DISPLAY_ID,
              fe.STATUS,
              fe.PROCESSING_START,
              fe.PROCESSING_END,
              fe.OPB_PERFORMING,
              fe.IS_INITIAL_TRANSFER,
              fe.CREATED_BY,
              COUNT(CASE WHEN efl.CATEGORY = 'REPORT'      THEN 1 END) AS REPORT_COUNT,
              COUNT(CASE WHEN efl.CATEGORY = 'OUTPUT_FILE' THEN 1 END) AS OUTPUT_FILE_COUNT
            FROM T_FUND_EVENT fe
            JOIN T_FUND f
              ON f.ID_FUND = fe.ID_FUND
            LEFT JOIN T_EVENT_FILE_LOG efl
              ON efl.ID_EVENT = fe.ID_EVENT
            WHERE 1=1
        """);

        count.append("""
            SELECT COUNT(*) 
            FROM T_FUND_EVENT fe
            JOIN T_FUND f
              ON f.ID_FUND = fe.ID_FUND
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();

        // Required fund code
        sql.append(" AND f.FUND_CODE = :fundCode");
        count.append(" AND f.FUND_CODE = :fundCode");
        params.put("fundCode", fundCode);

        // Optional filters (add only when non-null / non-empty)
        if (StringUtils.hasText(eventType)) {
            sql.append(" AND fe.EVENT_TYPE = :eventType");
            count.append(" AND fe.EVENT_TYPE = :eventType");
            params.put("eventType", eventType);
        }
        if (StringUtils.hasText(status)) {
            sql.append(" AND fe.STATUS = :status");
            count.append(" AND fe.STATUS = :status");
            params.put("status", status);
        }
        if (fromDate != null) {
            sql.append(" AND fe.PROCESSING_START >= :fromDate");
            count.append(" AND fe.PROCESSING_START >= :fromDate");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            sql.append(" AND fe.PROCESSING_START <= :toDate");
            count.append(" AND fe.PROCESSING_START <= :toDate");
            params.put("toDate", toDate);
        }

        // Grouping for aggregates
        sql.append("""
            GROUP BY 
              fe.ID_EVENT, f.FUND_CODE, fe.EVENT_TYPE, fe.EVENT_DISPLAY_ID, fe.STATUS,
              fe.PROCESSING_START, fe.PROCESSING_END, fe.OPB_PERFORMING, fe.IS_INITIAL_TRANSFER, fe.CREATED_BY
        """);

        // ORDER BY from Pageable (whitelisted)
        String orderBy = buildOrderBy(pageable);
        if (orderBy.isEmpty()) {
            orderBy = " ORDER BY fe.PROCESSING_START DESC"; // default
        }
        sql.append(orderBy);

        // Build queries
        Query dataQ = em.createNativeQuery(sql.toString());
        Query countQ = em.createNativeQuery(count.toString());

        // Bind parameters
        params.forEach((k, v) -> {
            dataQ.setParameter(k, v);
            countQ.setParameter(k, v);
        });

        // Pagination
        dataQ.setFirstResult((int) pageable.getOffset());
        dataQ.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQ.getResultList();
        List<FundEventSummaryDto> content = mapRows(rows);

        Number total = ((Number) countQ.getSingleResult());
        long totalElements = total.longValue();

        return new PageImpl<>(content, pageable, totalElements);
    }

    private String buildOrderBy(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return "";
        }
        StringBuilder ob = new StringBuilder(" ORDER BY ");
        List<String> parts = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            String prop = order.getProperty();
            String col = SORT_COLUMN_MAP.get(prop);
            if (col != null) {
                parts.add(col + " " + (order.isAscending() ? "ASC" : "DESC"));
            }
        });
        if (parts.isEmpty()) {
            return "";
        }
        ob.append(String.join(", ", parts));
        return ob.toString();
    }

    private List<FundEventSummaryDto> mapRows(List<Object[]> rows) {
        List<FundEventSummaryDto> list = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            UUID eventId              = ((UUID) r[0]);
            String fundCode           = (String) r[1];
            String eventType          = (String) r[2];
            String eventDisplayId     = (String) r[3];
            String status             = (String) r[4];
            LocalDateTime start       = r[5] == null ? null : ((Timestamp) r[5]).toLocalDateTime();
            LocalDateTime end         = r[6] == null ? null : ((Timestamp) r[6]).toLocalDateTime();
            BigDecimal opbPerforming     = r[7] == null ? null : toBigDecimal(r[7]);
            Boolean initialTransfer   = r[8] == null ? null : toBoolean(r[8]);
            String createdBy          = (String) r[9];
            int reportCount          = toInt(r[10]);
            int outputFileCount      = toInt(r[11]);
            

            List<String> actions = ActionResolver.resolve(status, start.toLocalDate());

            
			list.add(new FundEventSummaryDto(eventId, fundCode, eventType, eventDisplayId, status, start, end,
					opbPerforming, initialTransfer, reportCount, outputFileCount, createdBy, actions));
        }
        return list;
        }

    private boolean toBoolean(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        if (o instanceof String s) return "Y".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s);
        return false;
    }

    private int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof BigInteger bi) return bi.intValue();
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }
    

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
