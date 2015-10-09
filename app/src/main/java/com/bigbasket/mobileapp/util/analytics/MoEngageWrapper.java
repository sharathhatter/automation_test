package com.bigbasket.mobileapp.util.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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
            moEHelper.onStop(context);
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

    public static void onNewIntent(MoEHelper moEHelper, Activity context, Intent intent) {
        try {
            moEHelper.onNewIntent(context, intent);
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

    public static void onFragmentStart(MoEHelper moEHelper, Activity context, String fragmentName){
        try {
            moEHelper.onFragmentStart(context, fragmentName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onFragmentStop(MoEHelper moEHelper, Activity context, String fragmentName){
        try {
            moEHelper.onFragmentStop(context, fragmentName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void suppressInAppMessageHere(MoEHelper moEHelper){
        try {
            moEHelper.suppressInAppMessageHere(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setExistingUser(MoEHelper moEHelper, boolean isExistingUser){
        try {
            moEHelper.setExistingUser(isExistingUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
