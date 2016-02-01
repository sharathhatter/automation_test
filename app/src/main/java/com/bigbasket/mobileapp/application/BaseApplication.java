package com.bigbasket.mobileapp.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.adapter.communicationhub.PushNotificationListener;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.LeakCanaryObserver;
import com.bigbasket.mobileapp.util.MultiDexHandler;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.localytics.android.Localytics;
import com.localytics.android.LocalyticsActivityLifecycleCallbacks;
import com.moe.pushlibrary.MoEHelper;
import com.moengage.push.PushManager;
import com.newrelic.agent.android.NewRelic;
import com.newrelic.agent.android.logging.AgentLog;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

public class BaseApplication extends Application {

    private static Context sContext;

    public BaseApplication() {
        sContext = this;
    }

    public static Context getContext() {
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
        Context appContext = getApplicationContext();
        sContext = appContext;
        NewRelic.withApplicationToken(appContext.getString(R.string.new_relic_app_token))
                .start(appContext);
        Fabric.with(appContext, new Crashlytics());
        AuthParameters.reset();
        FacebookSdk.sdkInitialize(appContext);
        MoEHelper.APP_DEBUG = BuildConfig.DEBUG;
        initializeLeakCanary();
        if (!BuildConfig.DEBUG) {
            AdWordsConversionReporter.reportWithConversionId(appContext,
                    "963141508", "hfTqCLOjpWAQhL-hywM", "0.00", false);
        } else {
            //TODO: read localytics log enable state from dev config settings
            Localytics.setLoggingEnabled(false);
        }
        Picasso p = new Picasso.Builder(appContext)
                .memoryCache(new LruCache(getMemCacheSize()))
                .downloader(new OkHttp3Downloader(BigBasketApiAdapter.getHttpClient(this)))
                .build();
        Picasso.setSingletonInstance(p);
        PushManager.getInstance(this).setMessageListener(new PushNotificationListener());
        if (appContext.getFilesDir() != null) {
            registerActivityLifecycleCallbacks(
                    new LocalyticsActivityLifecycleCallbacks(appContext));
        } else {
            LocalyticsWrapper.HAS_NO_DIR = true;
        }
    }

    private int getMemCacheSize() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        boolean largeHeap = (getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap) {
            memoryClass = am.getLargeMemoryClass();
        }
        // Target ~10% of the available heap.
        return 1024 * 1024 * memoryClass / 10;
    }

}
