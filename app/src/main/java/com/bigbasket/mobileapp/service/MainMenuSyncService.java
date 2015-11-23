package com.bigbasket.mobileapp.service;

import android.content.Intent;

public class MainMenuSyncService extends AbstractDynamicPageSyncService {
    @Override
    protected void onHandleIntent(Intent intent) {
        syncDynamicScreen(MAIN_MENU);
    }
}
