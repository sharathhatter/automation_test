package com.bigbasket.mobileapp.application;

import android.app.ActivityManager;
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
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(
                    new LocalyticsActivityLifecycleCallbacks(this));
        } else {
            LocalyticsWrapper.integrate(this);
        }
        initializeLeakCanary();
        if (!BuildConfig.DEBUG) {
            AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),
                    "963141508", "hfTqCLOjpWAQhL-hywM", "0.00", false);
        } else {
            Localytics.setLoggingEnabled(true);
        }
        Picasso p = new Picasso.Builder(this)
                .memoryCache(new LruCache(getMemCacheSize()))
                .build();
        Picasso.setSingletonInstance(p);
    }

    private int getMemCacheSize() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        boolean largeHeap = (getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap && SDK_INT >= HONEYCOMB) {
            memoryClass = am.getLargeMemoryClass();
        }
        // Target ~10% of the available heap.
        return 1024 * 1024 * memoryClass / 10;
    }
}
