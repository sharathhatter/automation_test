package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.view.uiv3.FloatingBadgeCountView;

public interface FloatingBasketUIAware {
    void setViewBasketFloatingButton();

    @Nullable
    FloatingBadgeCountView getViewBasketFloatingButton();
}
