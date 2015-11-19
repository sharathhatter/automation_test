package com.bigbasket.mobileapp.util;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public final class LeakCanaryObserverImpl implements LeakCanaryObserver {
    public LeakCanaryObserverImpl() {
    }

    private RefWatcher refWatcher;

    @Override
    public void initializeWatcher(Application app) {
        if (refWatcher != null) {
            throw new IllegalStateException("LeakCanary watcher is already initialized");
        }
        refWatcher = LeakCanary.install(app);
    }

    @Override
    public void observe(Object object) {
        if (refWatcher != null) {
            refWatcher.watch(object);
        }
    }
}
