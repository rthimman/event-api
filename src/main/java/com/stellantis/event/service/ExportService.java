package com.stellantis.event.service;

import com.stellantis.event.entity.FundEventEntity;

public interface ExportService {
	void generateReftit(FundEventEntity event);
}
