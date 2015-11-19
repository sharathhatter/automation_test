package com.bigbasket.mobileapp.util;

import android.app.Application;

public interface LeakCanaryObserver {

    void initializeWatcher(Application app);

    void observe(Object object);

    class Factory {
        private static LeakCanaryObserver leakCanaryObserver;

        public static LeakCanaryObserver create() {
            leakCanaryObserver = new LeakCanaryObserverImpl();
            return leakCanaryObserver;
        }

        public static void observe(Object object) {
            if (leakCanaryObserver != null) {
                leakCanaryObserver.observe(object);
            }
        }
    }
}
