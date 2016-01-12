package com.bigbasket.mobileapp.util.analytics;

import android.app.Application;
import android.text.TextUtils;

import com.localytics.android.Localytics;
import com.newrelic.agent.android.NewRelic;

import java.util.Map;


public class LocalyticsWrapper {

    public static void setIdentifier(String key, String value) {
        try {
            Localytics.setIdentifier(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void integrate(Application application) {
        try {
            Localytics.integrate(application);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onPause() {
        try {
            Localytics.closeSession();
            Localytics.upload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onResume() {
        try {
            Localytics.openSession();
            Localytics.upload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tagScreen(String screenName) {
        try {
            if (!TextUtils.isEmpty(screenName))
                Localytics.tagScreen(screenName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tagEvent(String eventName, Map<String, String> eventAttribs) {
        try {
            Localytics.tagEvent(eventName, eventAttribs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tagEvent(String eventName, Map<String, String> eventAttribs, long customerValueIncrease) {
        try {
            Localytics.tagEvent(eventName, eventAttribs, customerValueIncrease);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
