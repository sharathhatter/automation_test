package com.bigbasket.mobileapp.util;

import android.app.Application;
import android.os.Build;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public final class LeakCanaryObserverImpl implements LeakCanaryObserver {
    public LeakCanaryObserverImpl() {
    }

    private RefWatcher refWatcher;

    @Override
    public void initializeWatcher(Application app) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (refWatcher != null) {
                throw new IllegalStateException("LeakCanary watcher is already initialized");
            }
            refWatcher = LeakCanary.install(app);
        }
    }

    @Override
    public void observe(Object object) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (refWatcher != null) {
                refWatcher.watch(object);
            }
        }
    }
}
