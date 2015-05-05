package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.melnykov.fab.FloatingActionButton;

public interface FloatingBasketUIAware {
    void setViewBasketFloatingButton();

    @Nullable
    FloatingActionButton getViewBasketFloatingButton();
}
