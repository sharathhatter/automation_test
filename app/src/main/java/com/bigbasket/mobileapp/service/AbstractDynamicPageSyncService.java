package com.bigbasket.mobileapp.service;

import android.app.IntentService;

import com.bigbasket.mobileapp.adapter.db.DynamicPageDbHelper;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.models.response.DynamicPageResponse;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;


public abstract class AbstractDynamicPageSyncService extends IntentService {
    public static final String HOME_PAGE = "home_page";
    public static final String MAIN_MENU = "main_menu";

    public AbstractDynamicPageSyncService() {
        super("AbstractDynamicPageSyncService");
    }

    protected void syncDynamicScreen(String dynamicScreenType) {
        String urlSuffix;
        if (dynamicScreenType.equals(HOME_PAGE)) {
            urlSuffix = "get-home-page/";
        } else {
            urlSuffix = "get-main-menu/";
        }
        urlSuffix += "?" + Constants.OS + "=android&" + Constants.APP_VERSION
                + "=" + DataUtil.getAppVersion(this);
        Request request = new Request.Builder()
                .url(MobileApiUrl.getMobileApiUrl(this) + urlSuffix)
                .build();
        OkHttpClient client = BigBasketApiAdapter.getHttpClient(this);
        String responseJson;
        int cacheDuration = 20; // Default
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                responseJson = response.body().string();
                // Parsing the json to extract duration for caching :(
                DynamicPageResponse dynamicPageResponse = new Gson().fromJson(responseJson, DynamicPageResponse.class);
                cacheDuration = dynamicPageResponse.apiResponseContent.cacheDuration;
            } else {
                responseJson = "";
            }
        } catch (IOException e) {
            responseJson = "";
        }
        DynamicPageDbHelper dynamicPageDbHelper = new DynamicPageDbHelper(this);
        dynamicPageDbHelper.save(dynamicScreenType, responseJson, cacheDuration);
    }
}
