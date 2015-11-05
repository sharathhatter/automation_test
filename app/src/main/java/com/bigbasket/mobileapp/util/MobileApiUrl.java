package com.bigbasket.mobileapp.util;

import android.content.Context;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.devconfig.DeveloperConfigs;

public final class MobileApiUrl {
    private static final String API_PATH = "/mapi/v2.3.0";

    private MobileApiUrl() {
    }

    public static String getMobileApiUrl(Context context){
        if(BuildConfig.DEBUG) {
            if(DeveloperConfigs.isDeveloper(context)) {
                return DeveloperConfigs.getMapiServerAddress(context) + API_PATH;
            }
        }

        return context.getString(R.string.pref_default_server_address) + API_PATH;

    }
}