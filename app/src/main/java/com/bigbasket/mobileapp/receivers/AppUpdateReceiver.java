package com.bigbasket.mobileapp.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.handler.AppDataSyncHandler;
import com.bigbasket.mobileapp.util.BBUtil;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

public class AppUpdateReceiver extends com.moe.pushlibrary.AppUpdateReceiver {

    @Override
    public void onReceive(Context con, Intent intent) {
        super.onReceive(con, intent);
        AppDataSyncHandler.reset(con);
        updateUserCredentialsWithEncryption();
    }

    public void updateUserCredentialsWithEncryption() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getContext());
        boolean hasBeenEncrypted = preferences.contains(Constants.ENCRYPTED);
        if (!hasBeenEncrypted) {
            boolean rememberMe = preferences.getBoolean(Constants.REMEMBER_ME_PREF, false);
            if (rememberMe) {
                SharedPreferences.Editor editor = preferences.edit();
                String passwd = preferences.getString(Constants.PASSWD_PREF, null);
                editor.putString(Constants.PASSWD_PREF, BBUtil.getEncryptedString(passwd));
                editor.putBoolean(Constants.ENCRYPTED, true);
                editor.apply();
            }
        }
    }
}
