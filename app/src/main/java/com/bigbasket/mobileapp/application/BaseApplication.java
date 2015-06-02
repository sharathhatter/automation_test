package com.bigbasket.mobileapp.application;

import android.app.Application;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.moe.pushlibrary.MoEHelper;

import io.fabric.sdk.android.Fabric;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AuthParameters.updateInstance(this);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        MoEHelper.APP_DEBUG = true;
        LocalyticsWrapper.integrate(this);
    }

}
