package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

public interface AnalyticsNavigationContextAware {
    String getCurrentNavigationContext();
    void setCurrentNavigationContext(@Nullable String nc);
    String getNextScreenNavigationContext();
    void setNextScreenNavigationContext(@Nullable String nc);
}
