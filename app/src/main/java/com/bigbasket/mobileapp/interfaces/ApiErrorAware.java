package com.bigbasket.mobileapp.interfaces;

import android.os.Bundle;
import android.support.annotation.Nullable;

public interface ApiErrorAware {

    void showApiErrorDialog(@Nullable CharSequence title, CharSequence message);

    void showApiErrorDialog(@Nullable CharSequence title, CharSequence message, boolean finish);

    void showApiErrorDialog(@Nullable CharSequence title, CharSequence message, int requestCode, Bundle data);

    void showApiErrorDialog(@Nullable CharSequence title, CharSequence message, int resultCode);

}
