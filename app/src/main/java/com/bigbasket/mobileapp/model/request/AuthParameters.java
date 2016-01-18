package com.bigbasket.mobileapp.model.request;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.util.Constants;

public class AuthParameters {
    private static final Object lock = new Object();
    private static volatile AuthParameters authParameters;
    private String visitorId;
    private String bbAuthToken;
    private String osVersion;
    private String mid;
    private String memberEmail;
    private String memberFullName;
    private String firstName;
    private String cityId;
    private boolean isMoEngageEnabled;
    private boolean isLocalyticsEnabled;
    private boolean isFBLoggerEnabled;
    private boolean isKirana;
    private boolean isMultiCityEnabled;
    private boolean isNewRelicEnabled;

    private AuthParameters(Context context) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefer != null) {
            visitorId = prefer.getString(Constants.VISITOR_ID_KEY, null);
            if (TextUtils.isEmpty(visitorId)) {
                visitorId = prefer.getString(Constants.OLD_VISITOR_ID_KEY, "");
            }
            bbAuthToken = prefer.getString(Constants.BBTOKEN_KEY, null);
            if (TextUtils.isEmpty(bbAuthToken)) {
                bbAuthToken = prefer.getString(Constants.OLD_BBTOKEN_KEY, "");
            }
            mid = prefer.getString(Constants.MID_KEY, "");
            memberEmail = prefer.getString(Constants.MEMBER_EMAIL_KEY, "");
            memberFullName = prefer.getString(Constants.MEMBER_FULL_NAME_KEY, "");
            osVersion = prefer.getString(Constants.OS_PREFERENCE_KEY, "");
            isNewRelicEnabled = prefer.getBoolean(Constants.ENABLE_NEWRELIC, false);
            isMoEngageEnabled = prefer.getBoolean(Constants.ENABLE_MOENGAGE, true);
            isLocalyticsEnabled = prefer.getBoolean(Constants.ENABLE_LOCALYTICS, true);
            isFBLoggerEnabled = prefer.getBoolean(Constants.ENABLE_FB_LOGGER, true);
            firstName = prefer.getString(Constants.FIRST_NAME_PREF, "");
            isKirana = prefer.getBoolean(Constants.IS_KIRANA, false);
            isMultiCityEnabled = prefer.getBoolean(Constants.IS_MULTICITY_ENABLED, false);
            cityId = prefer.getString(Constants.CITY_ID, "1");
        }
    }

    public static void reset() {
        authParameters = null;
        BigBasketApiAdapter.reset();
    }

    public static void resetCity(String cityId) {
        if (authParameters != null) {
            authParameters.cityId = cityId;
        }
    }

    public static AuthParameters getInstance(Context context) {
        AuthParameters localInstance = authParameters;
        if (localInstance == null) {
            synchronized (lock) {
                localInstance = authParameters;
                if (localInstance == null) {
                    localInstance = new AuthParameters(context);
                    authParameters = localInstance;
                }
            }
        }
        return localInstance;
    }

    public String getVisitorId() {
        return visitorId != null ? visitorId : "";
    }

    public String getBbAuthToken() {
        return bbAuthToken != null ? bbAuthToken : "";
    }

    public String getOsVersion() {
        return osVersion != null ? osVersion : "";
    }

    public String getMid() {
        return mid != null ? mid : "";
    }

    public String getMemberEmail() {
        return memberEmail != null ? memberEmail : "";
    }

    public boolean isKirana() {
        return isKirana;
    }

    public String getFirstName() {
        if (firstName != null) {
            return firstName;
        } else if (memberFullName != null) {
            return memberFullName.split(" ")[0];
        }
        return "";
    }

    public String getMemberFullName() {
        return memberFullName != null ? memberFullName : "";
    }

    public boolean isAuthTokenEmpty() {
        return TextUtils.isEmpty(getBbAuthToken());
    }

    public boolean isMoEngageEnabled() {
        return isMoEngageEnabled;
    }

    public void setAppCapability(boolean isNewRelicEnabled, boolean isMoEngaleEnabled,
                                 boolean isLocalyticsEnabled,
                                 boolean isFBLoggerEnabled,
                                 boolean isMultiCityEnabled,
                                 Context context) {
        if (context == null) return;
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean(Constants.ENABLE_NEWRELIC, isNewRelicEnabled);
        editor.putBoolean(Constants.ENABLE_MOENGAGE, isMoEngaleEnabled);
        editor.putBoolean(Constants.ENABLE_LOCALYTICS, isLocalyticsEnabled);
        editor.putBoolean(Constants.ENABLE_FB_LOGGER, isFBLoggerEnabled);
        editor.putBoolean(Constants.IS_MULTICITY_ENABLED, isMultiCityEnabled);
        editor.apply();
        this.isNewRelicEnabled = isNewRelicEnabled;
        this.isMoEngageEnabled = isMoEngaleEnabled;
        this.isLocalyticsEnabled = isLocalyticsEnabled;
        this.isFBLoggerEnabled = isFBLoggerEnabled;
        this.isMultiCityEnabled = isMultiCityEnabled;
    }

    public String getCityId() {
        if (cityId == null) return "1";
        return cityId;
    }

    public boolean isNewRelicEnabled() {
        return isNewRelicEnabled;
    }

    public boolean isLocalyticsEnabled() {
        return isLocalyticsEnabled;
    }

    public boolean isFBLoggerEnabled() {
        return isFBLoggerEnabled;
    }

    public boolean isMultiCityEnabled() {
        return isMultiCityEnabled;
    }
}
