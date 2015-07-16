package com.bigbasket.mobileapp.interfaces;


import android.content.Intent;

public interface OnBasketChangeListener {
    void onBasketChanged(Intent data);

    void markBasketChanged(Intent data);
}
