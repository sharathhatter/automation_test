package com.bigbasket.mobileapp.application;

import android.app.Application;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.moe.pushlibrary.MoEHelper;

import io.fabric.sdk.android.Fabric;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AuthParameters.reset();
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        MoEHelper.APP_DEBUG = BuildConfig.DEBUG;
        LocalyticsWrapper.integrate(this);
        if(!BuildConfig.DEBUG) {
            AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),
                    "990877306", "wqThCIz2ql8Q-qy-2AM", "0.00", false);
        }
    }
}
