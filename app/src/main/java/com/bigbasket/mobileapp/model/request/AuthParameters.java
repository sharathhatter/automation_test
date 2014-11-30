package com.bigbasket.mobileapp.model.request;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.util.Constants;

public class AuthParameters {
    private static AuthParameters authParameters;

    private String visitorId;
    private String bbAuthToken;
    private String osVersion;
    private String mid;
    private String memberEmail;
    private String memberFullName;
    private boolean isKonotorEnabled;

    public static void updateInstance(Context context) {
        authParameters = new AuthParameters(context);
        BigBasketApiAdapter.refreshBigBasketApiService(context);
    }

    public static AuthParameters getInstance(Context context) {
        if (authParameters == null) {
            authParameters = new AuthParameters(context);
        }
        return authParameters;
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

    public String getMemberFullName() {
        return memberFullName != null ? memberFullName : "";
    }

    public boolean isAuthTokenEmpty() {
        return TextUtils.isEmpty(getBbAuthToken());
    }

    public boolean isKonotorEnabled() {
        return isKonotorEnabled;
    }

    public void setKonotorEnabled(boolean isKonotorEnabled, Context context) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean(Constants.ENABLE_KONOTOR, isKonotorEnabled);
        editor.commit();
        this.isKonotorEnabled = isKonotorEnabled;
    }

    private AuthParameters(Context context) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefer != null) {
            visitorId = prefer.getString(Constants.VISITOR_ID_KEY, "");
            if (visitorId == null || visitorId.length() == 0)
                visitorId = prefer.getString(Constants.OLD_VISITOR_ID_KEY, "");

            bbAuthToken = prefer.getString(Constants.BBTOKEN_KEY, "");
            if (bbAuthToken == null || bbAuthToken.length() == 0)
                bbAuthToken = prefer.getString(Constants.OLD_BBTOKEN_KEY, "");

            mid = prefer.getString(Constants.MID_KEY, "");
            memberEmail = prefer.getString(Constants.MEMBER_EMAIL_KEY, "");
            memberFullName = prefer.getString(Constants.MEMBER_FULL_NAME_KEY, "");
            osVersion = prefer.getString(Constants.OS_PREFERENCE_KEY, "");
            isKonotorEnabled = prefer.getBoolean(Constants.ENABLE_KONOTOR, false);
        }
    }
}
