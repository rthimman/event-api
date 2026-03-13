package com.stellantis.event.service;

import com.stellantis.event.entity.FundEventEntity;

public interface AccountingService {
    void generateAccounting(FundEventEntity event);
}

