package com.bigbasket.mobileapp.util.analytics;

import android.content.Context;
import android.text.TextUtils;

import com.localytics.android.Localytics;

import java.util.Map;

/**
 * Created by jugal on 11/2/15.
 */
public class LocalyticsWrapper {

    public static void setIdentifier(String key, String value) {
        try {
            Localytics.setIdentifier(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void integrate(Context context) {
        try {
            Localytics.integrate(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onPause() {
        try {
            Localytics.closeSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Localytics.upload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onResume(String screenName) {
        try {
            Localytics.openSession();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!TextUtils.isEmpty(screenName))
                Localytics.tagScreen(screenName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Localytics.upload();
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
