
package com.stellantis.event.entity;

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
@Table(name = "t_event_file_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventFileLogEntity {

	@Id
	@GeneratedValue
	@Column(name = "id_file", columnDefinition = "uuid")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_event", nullable = false, foreignKey = @ForeignKey(name = "t_event_file_log_id_event_fkey"))
	private FundEventEntity fundEventEntity;

	@Column(name = "file_name", length = 255, nullable = false)
	private String fileName;

	@Column(name = "file_type", length = 30, nullable = false)
	private String fileType;

	// @Enumerated(EnumType.STRING)
	@Column(name = "category", length = 20, nullable = false)
	private String category;

	@Column(name = "report_type", length = 50)
	private String reportType;

	@Column(name = "storage_path", length = 500, nullable = false)
	private String storagePath;

	@Column(name = "size_bytes")
	private Long sizeBytes;

	@Column(name = "mime_type", length = 100)
	private String mimeType = "application/octet-stream";

	@Column(name = "is_tiers_restricted")
	private Boolean tiersRestricted = false;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	// ---------- ENUM ------------

	public enum FileCategory {
		REPORT, OUTPUT_FILE
	}
}
