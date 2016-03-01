package com.bigbasket.mobileapp.receivers;

import android.content.Context;
import android.content.Intent;

import com.bigbasket.mobileapp.handler.AppDataSyncHandler;

public class AppUpdateReceiver extends com.moe.pushlibrary.AppUpdateReceiver {

    @Override
    public void onReceive(Context con, Intent intent) {
        super.onReceive(con, intent);
        AppDataSyncHandler.reset(con);
    }
}
