package com.stellantis.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExecuteActionRequestDto {
	@NotBlank
	private String action;
}
