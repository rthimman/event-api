package com.stellantis.event.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stellantis.event.entity.FundEntity;

public interface FundRepository extends JpaRepository<FundEntity, UUID> {

	boolean existsByFundCode(String fundCode);
}
