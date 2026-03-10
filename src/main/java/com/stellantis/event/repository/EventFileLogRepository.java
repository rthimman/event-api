package com.stellantis.event.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.stellantis.event.entity.EventFileLogEntity;

public interface EventFileLogRepository extends JpaRepository<EventFileLogEntity, UUID> {

 List<EventFileLogEntity> findByFundEventEntity_IdAndCategoryOrderByCreatedAtAsc(
         UUID eventId, String category);

 // If you prefer DESC:
 // List<EventFileLogEntity> findByFundEventEntity_IdAndCategoryOrderByCreatedAtDesc(
 //        UUID eventId, String category);
}