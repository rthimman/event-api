package com.stellantis.event.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse {

	private List<FundEventSummaryDto> content;
	private long totalElements;
	private int totalPages;
	private int number;
	private int size;
	private boolean first;
	private boolean last;

}
