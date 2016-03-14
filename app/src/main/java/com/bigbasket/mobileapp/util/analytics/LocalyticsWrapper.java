package com.bigbasket.mobileapp.util.analytics;

import android.text.TextUtils;

import com.localytics.android.Localytics;
import com.newrelic.agent.android.NewRelic;

import java.util.Map;


public class LocalyticsWrapper {

    public static boolean HAS_NO_DIR = false;  // If context.getFilesDir() returns null, then disable localytics

    public static void setIdentifier(String key, String value) {
        if (HAS_NO_DIR) return;
        try {
            Localytics.setIdentifier(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onPause() {
        if (HAS_NO_DIR) return;
        try {
            Localytics.closeSession();
            Localytics.upload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onResume() {
        if (HAS_NO_DIR) return;
        try {
            Localytics.openSession();
            Localytics.upload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tagScreen(String screenName) {
        if (HAS_NO_DIR) return;
        try {
            if (!TextUtils.isEmpty(screenName))
                Localytics.tagScreen(screenName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tagEvent(String eventName, Map<String, String> eventAttribs) {
        if (HAS_NO_DIR) return;
        try {
            Localytics.tagEvent(eventName, eventAttribs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tagEvent(String eventName, Map<String, String> eventAttribs, long customerValueIncrease) {
        if (HAS_NO_DIR) return;
        try {
            Localytics.tagEvent(eventName, eventAttribs, customerValueIncrease);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
