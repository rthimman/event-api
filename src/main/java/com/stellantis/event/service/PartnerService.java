package com.stellantis.event.service;

import com.stellantis.event.entity.FundEventEntity;

public interface PartnerService {

	String sendToPartner(FundEventEntity event);
}
