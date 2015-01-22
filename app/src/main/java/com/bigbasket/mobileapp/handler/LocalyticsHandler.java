package com.bigbasket.mobileapp.handler;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.support.annotation.Nullable;

import com.localytics.android.LocalyticsActivityLifecycleCallbacks;
import com.localytics.android.LocalyticsAmpSession;

public class LocalyticsHandler {

    private static LocalyticsHandler localyticsHandler;
    private LocalyticsAmpSession localyticsAmpSession;
    public static final String CUSTOMER_MOBILE = "Mobile";
    public static final String CUSTOMER_GENDER = "Gender";
    public static final String CUSTOMER_BDAY = "Birthday";
    public static final String CUSTOMER_REGISTERED_ON = "Registered On";
    public static final String CUSTOMER_HUB = "Hub";
    public static final String CUSTOMER_CITY = "City";

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private LocalyticsHandler(Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            localyticsAmpSession = new LocalyticsAmpSession(application.getApplicationContext());
            application.registerActivityLifecycleCallbacks(new LocalyticsActivityLifecycleCallbacks(localyticsAmpSession));
        }
    }

    public static LocalyticsHandler getInstance(Application application) {
        if (localyticsHandler == null) {
            localyticsHandler = new LocalyticsHandler(application);
        }
        return localyticsHandler;
    }

    @Nullable
    public LocalyticsAmpSession getSession() {
        return localyticsAmpSession;
    }
}
