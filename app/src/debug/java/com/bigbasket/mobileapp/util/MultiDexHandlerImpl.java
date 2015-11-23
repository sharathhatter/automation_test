package com.bigbasket.mobileapp.util;

import android.app.Application;
import android.support.multidex.MultiDex;

public class MultiDexHandlerImpl implements MultiDexHandler {

    @Override
    public void install(Application app) {
        MultiDex.install(app);
    }
}
