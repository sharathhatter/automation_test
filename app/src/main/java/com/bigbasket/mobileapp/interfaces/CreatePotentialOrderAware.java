package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;

public interface CreatePotentialOrderAware {
    void onPotentialOrderCreated(CreatePotentialOrderResponseContent createPotentialOrderResponseContent);

    void postOrderQc(CreatePotentialOrderResponseContent createPotentialOrderResponseContent);
}
