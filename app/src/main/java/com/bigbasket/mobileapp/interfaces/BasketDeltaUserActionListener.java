package com.bigbasket.mobileapp.interfaces;

public interface BasketDeltaUserActionListener {
    void onUpdateBasket(String addressId, String lat, String lng);
    void onNoBasketUpdate();
}
