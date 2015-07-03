package com.bigbasket.mobileapp.util.analytics;

import android.app.Activity;
import android.content.Context;

import com.moe.pushlibrary.MoEHelper;

import org.json.JSONObject;

public class MoEngageWrapper {

    public static MoEHelper getMoHelperObj(Context context) {
        try {
            return new MoEHelper(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setUserAttribute(MoEHelper moEHelper, String key, String value) {
        try {
            moEHelper.setUserAttribute(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setUserAttribute(MoEHelper moEHelper, String key, boolean isLoggedIn) {
        try {
            moEHelper.setUserAttribute(key, isLoggedIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void onStart(MoEHelper moEHelper, Activity context) {
        try {
            moEHelper.onStart(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onStop(MoEHelper moEHelper, Activity context) {
        try {
            moEHelper.onStart(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onPause(MoEHelper moEHelper, Activity context) {
        try {
            moEHelper.onPause(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onResume(MoEHelper moEHelper, Activity context) {
        try {
            moEHelper.onResume(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackEvent(MoEHelper moEHelper, String eventName, JSONObject analyticsJsonObj) {
        try {
            moEHelper.trackEvent(eventName, analyticsJsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
