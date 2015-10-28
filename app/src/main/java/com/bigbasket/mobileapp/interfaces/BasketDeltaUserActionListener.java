package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

public interface BasketDeltaUserActionListener {
    void onUpdateBasket(String addressId, String lat, String lng, @Nullable String area);

    void onNoBasketUpdate();
}
