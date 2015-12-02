package com.bigbasket.mobileapp.service;

import android.content.Intent;

public class HomePageSyncService extends AbstractDynamicPageSyncService {
    @Override
    protected void onHandleIntent(Intent intent) {
        syncDynamicScreen(HOME_PAGE);
    }
}
