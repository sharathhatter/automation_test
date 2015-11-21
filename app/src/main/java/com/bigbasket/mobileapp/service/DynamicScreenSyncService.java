package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.content.Intent;

import com.bigbasket.mobileapp.adapter.db.DynamicScreenAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;


public class DynamicScreenSyncService extends IntentService {
    public static final String HOME_PAGE = "home_page";
    public static final String MAIN_MENU = "main_menu";

    public static final int HOME_PAGE_ID = 1;
    public static final int MAIN_MENU_ID = 2;

    public DynamicScreenSyncService() {
        super("DynamicScreenSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action == null) return;
            syncDynamicScreen(action);
        }
    }

    private void syncDynamicScreen(String dynamicScreenType) {
        String urlSuffix;
        if (dynamicScreenType.equals(HOME_PAGE)) {
            urlSuffix = "get-home-page/";
        } else {
            urlSuffix = "get-main-menu/";
        }
        Request request = new Request.Builder()
                .url(MobileApiUrl.getMobileApiUrl(this) + urlSuffix)
                .build();
        OkHttpClient client = BigBasketApiAdapter.getHttpClient(this);
        String responseJson;
        try {
            Response response = client.newCall(request).execute();
            responseJson = response.body().string();
        } catch (IOException e) {
            responseJson = "";
        }
        DynamicScreenAdapter dynamicScreenAdapter = new DynamicScreenAdapter(this);
        dynamicScreenAdapter.insert(dynamicScreenType, responseJson);
    }
}
