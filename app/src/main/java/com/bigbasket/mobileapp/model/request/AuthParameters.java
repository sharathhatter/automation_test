package com.bigbasket.mobileapp.model.request;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.util.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AuthParameters {
    private static AuthParameters authParameters;
    public static final int FIRST_TIME_COOKIE_EXPIRY_IN_DAYS = 15;

    private String visitorId;
    private String bbAuthToken;
    private String osVersion;
    private String mid;
    private String memberEmail;
    private String memberFullName;
    private boolean isKonotorEnabled;
    private boolean isMoEngaleEnabled;
    private boolean isLocalyticsEnabled;

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

    public boolean isMoEngaleEnabled() {
        return isMoEngaleEnabled;
    }

    public void setMoEngaleLocaliticsEnabled(boolean isMoEngaleEnabled,
                                             boolean isLocalyticsEnabled, Context context) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean(Constants.ENABLE_MOENGAGE, isMoEngaleEnabled);
        editor.putBoolean(Constants.ENABLE_LOCALYTICS, isLocalyticsEnabled);
        editor.commit();
        this.isMoEngaleEnabled = isMoEngaleEnabled;
        this.isLocalyticsEnabled = isLocalyticsEnabled;
    }

    public boolean isLocalyticsEnabled() {
        return isLocalyticsEnabled;
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
            isMoEngaleEnabled = prefer.getBoolean(Constants.ENABLE_MOENGAGE, false);
        }
    }

    public static boolean isFirstTimeVisitor(Context context) {
        if (!AuthParameters.getInstance(context).isAuthTokenEmpty()) return false;
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        String firstTimeCookieTimeStamp = prefer.getString(Constants.FIRST_TIME_COOKIE_TIME_STAMP, null);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date now = new Date();
        if (TextUtils.isEmpty(firstTimeCookieTimeStamp)) {
            setFirstTimeVisitor(context, now, simpleDateFormat);
            return true;
        }
        try {
            Date createdOnDate = simpleDateFormat.parse(firstTimeCookieTimeStamp);
            long days = TimeUnit.DAYS.convert(now.getTime() - createdOnDate.getTime(),
                    TimeUnit.MILLISECONDS);
            return days <= FIRST_TIME_COOKIE_EXPIRY_IN_DAYS;
        } catch (ParseException e) {
            setFirstTimeVisitor(context, now, simpleDateFormat);
            return true;
        }
    }

    private static void setFirstTimeVisitor(Context context,
                                            Date now, SimpleDateFormat simpleDateFormat) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Constants.FIRST_TIME_COOKIE_TIME_STAMP,
                simpleDateFormat.format(now));
        editor.commit();
    }
}
