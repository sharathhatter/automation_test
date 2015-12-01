package com.bigbasket.mobileapp.application;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.LeakCanaryObserver;
import com.bigbasket.mobileapp.util.MultiDexHandler;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.localytics.android.Localytics;
import com.localytics.android.LocalyticsActivityLifecycleCallbacks;
import com.moe.pushlibrary.MoEHelper;

import io.fabric.sdk.android.Fabric;

public class BaseApplication extends Application {

    private static Context sContext;

    public BaseApplication() {
        sContext = this;
    }

    public static Context getsContext() {
        return sContext;
    }

    private void initializeLeakCanary() {
        if (BuildConfig.DEBUG) {
            LeakCanaryObserver leakCanaryObserver = LeakCanaryObserver.Factory.create();
            leakCanaryObserver.initializeWatcher(this);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (BuildConfig.DEBUG) {
            MultiDexHandler.Factory.create().install(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AuthParameters.reset();
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        MoEHelper.APP_DEBUG = BuildConfig.DEBUG;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(
                    new LocalyticsActivityLifecycleCallbacks(this));
        } else {
            LocalyticsWrapper.integrate(this);
        }
        initializeLeakCanary();
        if (!BuildConfig.DEBUG) {
            AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),
                    "990877306", "wqThCIz2ql8Q-qy-2AM", "0.00", false);
        } else {
            Localytics.setLoggingEnabled(true);
        }
    }
}
