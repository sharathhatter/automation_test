package com.bigbasket.mobileapp.devconfig;

/**
 * Created by bigbasket on 5/11/15.
 */
public interface OnConfigChangeListener {
    void onServerChanged(String newServer);
    void onHttpLoggingLevelChanged(String newValue);
}
