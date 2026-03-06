package com.stellantis.event.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_country")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryEntity {

    @Id
    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "country_code_iso3", length = 3)
    private String countryCodeIso3;

    @Column(name = "country_name_en", length = 100, nullable = false)
    private String countryNameEn;

    @Column(name = "country_name_local", length = 100)
    private String countryNameLocal;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {

        if (isActive == null) {
            isActive = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
