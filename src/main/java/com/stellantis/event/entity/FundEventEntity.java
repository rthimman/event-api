package com.stellantis.event.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "t_fund_event",
        schema = "public",
        indexes = {
                @Index(name = "idx_fund_event_active_today", columnList = "id_fund,status,created_at"),
                @Index(name = "idx_fund_event_date", columnList = "event_date"),
                @Index(name = "idx_fund_event_fund", columnList = "id_fund"),
                @Index(name = "idx_fund_event_fund_status", columnList = "id_fund,status"),
                @Index(name = "idx_fund_event_status", columnList = "status"),
                @Index(name = "idx_fund_event_type", columnList = "event_type"),
                @Index(name = "uk_event_display_id", columnList = "event_display_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundEventEntity {

    @Id
    @GeneratedValue
    @Column(name = "id_event", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fund", foreignKey = @ForeignKey(name = "t_fund_event_id_fund_fkey"))
    private FundEntity fundEntity;

    //@Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 20, nullable = false)
    private EventType eventType;

    @Column(name = "is_initial_transfer")
    private Boolean initialTransfer = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private EventStatus status;

    @Column(name = "event_display_id", length = 20, unique = true)
    private String eventDisplayId;

    @Column(name = "event_date")
    private LocalDate eventDate = LocalDate.now();

    @Column(name = "target_amount", precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "selected_count")
    private Integer selectedCount;

    @Column(name = "selected_total_amount", precision = 15, scale = 2)
    private BigDecimal selectedTotalAmount;

    @Column(name = "excluded_count")
    private Integer excludedCount;

    @Column(name = "opb_performing", precision = 15, scale = 2)
    private BigDecimal opbPerforming;

    @Column(name = "buyback_count")
    private Integer buybackCount;

    @Column(name = "buyback_total_amount", precision = 15, scale = 2)
    private BigDecimal buybackTotalAmount;

    @Column(name = "buyback_blocked_count")
    private Integer buybackBlockedCount;

    @Column(name = "processing_start")
    private LocalDateTime processingStart;

    @Column(name = "processing_end")
    private LocalDateTime processingEnd;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "validated_by", length = 100)
    private String validatedBy;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "sent_to_partner_at")
    private LocalDateTime sentToPartnerAt;

    @Column(name = "sent_to_partner_by", length = 100)
    private String sentToPartnerBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ---------- ENUMS ------------

    public enum EventType {
        REPLENISHMENT,
        DAILY,
        MONTHLY_DAILY_BM,
        MONTHLY_MONTHLY_BM
    }

    public enum EventStatus {
        IN_PROGRESS,
        FINISHED,
        VALIDATED,
        CONFIRMED,
        CANCELLED
    }
}
