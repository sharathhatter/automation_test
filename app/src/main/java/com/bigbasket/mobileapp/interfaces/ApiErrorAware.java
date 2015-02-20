package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

public interface ApiErrorAware {

    public void showApiErrorDialog(@Nullable String title, String message);

    public void showApiErrorDialog(@Nullable String title, String message, boolean finish);

    public void showApiErrorDialog(@Nullable String title, String message, String sourceName, Object valuePassed);

    public void showApiErrorDialog(@Nullable String title, String message, int resultCode);

}
