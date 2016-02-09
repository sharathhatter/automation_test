package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.text.TextUtils;

import com.bigbasket.mobileapp.adapter.db.DynamicPageDbHelper;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.models.response.DynamicPageResponse;
import com.bigbasket.mobileapp.util.CompressUtil;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.crashlytics.android.Crashlytics;
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
        String appVersion = DataUtil.getAppVersion(this);
        int debugOrBetaIndex = appVersion.lastIndexOf("-");
        if(debugOrBetaIndex > 0){
            appVersion = appVersion.substring(0, debugOrBetaIndex);
        }
        urlSuffix += "?" + Constants.OS + "=android&" + Constants.APP_VERSION
                + "=" + appVersion;
        Request request = new Request.Builder()
                .url(MobileApiUrl.getMobileApiUrl(this) + urlSuffix)
                .build();
        OkHttpClient client = BigBasketApiAdapter.getHttpClient(this);
        String responseJson;
        int cacheDuration = 20; // Default
        DynamicPageResponse dynamicPageResponse = null;
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                responseJson = response.body().string();
                // Parsing the json to extract duration for caching :(
                dynamicPageResponse = new Gson().fromJson(responseJson,
                        DynamicPageResponse.class);
                if (dynamicPageResponse.status == 0) {
                    cacheDuration = dynamicPageResponse.apiResponseContent.cacheDuration;
                } else {
                    responseJson = null;
                }
            } else {
                responseJson = null;
            }
        } catch (IOException e) {
            responseJson = null;
        }
        byte[] compressedResponse = null;
        DynamicPageDbHelper dynamicPageDbHelper = new DynamicPageDbHelper(this);
        if (!TextUtils.isEmpty(responseJson) && dynamicPageResponse != null
                && dynamicPageResponse.apiResponseContent.sectionData != null
                && dynamicPageResponse.apiResponseContent.sectionData.getSections() != null
                && !dynamicPageResponse.apiResponseContent.sectionData.getSections().isEmpty()) {
            try {
                compressedResponse = CompressUtil.gzipCompress(responseJson);
            } catch (IOException e) {
                compressedResponse = null;
                Crashlytics.logException(e);
            }
        }
        if(compressedResponse != null) {
            dynamicPageDbHelper.save(dynamicScreenType, compressedResponse, cacheDuration);
        }
    }
}
