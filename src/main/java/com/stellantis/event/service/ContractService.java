package com.stellantis.event.service;

import java.util.UUID;

public interface ContractService {

	void releaseContractsForEvent(UUID eventId);
}
