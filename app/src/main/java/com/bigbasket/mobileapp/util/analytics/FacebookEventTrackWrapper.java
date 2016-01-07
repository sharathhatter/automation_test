package com.bigbasket.mobileapp.util.analytics;

import android.content.Context;
import android.os.Bundle;

import com.facebook.appevents.AppEventsLogger;

public class FacebookEventTrackWrapper {

    public static void activateApp(Context context) {
        try {
            AppEventsLogger.activateApp(context);//, context.getString(R.string.fb_app_id));
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void deactivateApp(Context context) {
        try {
            AppEventsLogger.deactivateApp(context);//, context.getString(R.string.fb_app_id));
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void logAppEvent(AppEventsLogger logger,
                                   String eventName, Bundle eventBundle) {
        try {
            logger.logEvent(eventName, eventBundle);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void logAppEvent(AppEventsLogger logger, String eventName,
                                   double valueToSum, Bundle bundleAttr) {
        try {
            logger.logEvent(eventName, valueToSum, bundleAttr);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void logAppEvent(AppEventsLogger logger,
                                   String eventName) {
        try {
            logger.logEvent(eventName);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
