package com.bigbasket.mobileapp.util;

import android.app.Application;

public interface MultiDexHandler {
    void install(Application app);

    class Factory {
        public static MultiDexHandler create() {
            return new MultiDexHandlerImpl();
        }
    }
}
