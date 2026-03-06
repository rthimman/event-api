package com.stellantis.event.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_fund")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundEntity {

    @Id
    @GeneratedValue
    @Column(name = "id_fund", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "description", length = 100, nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "country_code",
            referencedColumnName = "country_code",
            foreignKey = @ForeignKey(name = "t_fund_country_code_fkey")
    )
    private CountryEntity countryEntity;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    //@Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "product_code", length = 30)
    private String productCode;

    @Column(name = "fund_code", length = 20)
    private String fundCode;

    @Column(name = "fund_type", length = 20)
    private String fundType;

    @Column(name = "legal_entity", length = 100)
    private String legalEntity;

    @Column(name = "fund_manager", length = 100)
    private String fundManager;

    @Column(name = "entity_code", length = 10)
    private String entityCode;

    @Column(name = "sub_ledger_code", length = 30)
    private String subLedgerCode;

    @Column(name = "gl_account_cession", length = 20)
    private String glAccountCession;

    @Column(name = "issuer_code", length = 10)
    private String issuerCode;

    @Column(name = "eurefi_securitized_entity", length = 100)
    private String eurefiSecuritizedEntity;

    @Column(name = "bis_entity", length = 100)
    private String bisEntity;

    @Column(name = "cas_account_flag")
    private Boolean casAccountFlag = false;

    @Column(name = "maximum_exposure", precision = 15, scale = 2)
    private BigDecimal maximumExposure;

    @Column(name = "minimum_exposure", precision = 15, scale = 2)
    private BigDecimal minimumExposure;

    @Column(name = "target_yield", precision = 8, scale = 4)
    private BigDecimal targetYield;

    @Column(name = "inception_date")
    private LocalDate inceptionDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "last_review_date")
    private LocalDate lastReviewDate;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "initial_portfolio_amount", precision = 15, scale = 2)
    private BigDecimal initialPortfolioAmount;

    @Column(name = "outstanding_principal_balance", precision = 15, scale = 2)
    private BigDecimal outstandingPrincipalBalance;

    @Column(name = "is_retention_fund")
    private Boolean retentionFund = false;

    @Column(name = "retention_fund_code", length = 20)
    private String retentionFundCode;

    @Column(name = "retention_fund_amount", precision = 15, scale = 2)
    private BigDecimal retentionFundAmount;

    @Column(name = "servicing_fee_rate", precision = 8, scale = 6)
    private BigDecimal servicingFeeRate = new BigDecimal("0.000900");

    @Column(name = "liquidation_date")
    private LocalDate liquidationDate;

    @Column(name = "liquidated_by", length = 100)
    private String liquidatedBy;

    @Column(name = "created_by", length = 20)
    private String createdBy;

    @Column(name = "updated_by", length = 20)
    private String updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ---------- ENUM ------------

    public enum FundStatus {
        DRAFT,
        ACTIVE,
        REVOLVING,
        LIQUIDATED,
        CLOSED
    }
}

