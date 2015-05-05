package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.melnykov.fab.FloatingBadgeCountView;

public interface FloatingBasketUIAware {
    void setViewBasketFloatingButton();

    @Nullable
    FloatingBadgeCountView getViewBasketFloatingButton();

    void setViewBasketButtonStateOnActivityResume();
}
