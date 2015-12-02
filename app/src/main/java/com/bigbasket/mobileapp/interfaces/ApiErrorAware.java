package com.bigbasket.mobileapp.interfaces;

import android.os.Bundle;
import android.support.annotation.Nullable;

public interface ApiErrorAware {

    void showApiErrorDialog(@Nullable String title, String message);

    void showApiErrorDialog(@Nullable String title, String message, boolean finish);

    void showApiErrorDialog(@Nullable String title, String message, int requestCode, Bundle data);

    void showApiErrorDialog(@Nullable String title, String message, int resultCode);

}
