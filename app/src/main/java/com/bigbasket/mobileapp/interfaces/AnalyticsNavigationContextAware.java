package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

public interface AnalyticsNavigationContextAware {
    String getPreviousScreenName();

    void setPreviousScreenName(@Nullable String nc);

    String getCurrentScreenName();

    void setCurrentScreenName(@Nullable String nc);
}
