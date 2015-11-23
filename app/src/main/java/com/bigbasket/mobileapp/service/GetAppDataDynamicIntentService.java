package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.content.Intent;

import com.bigbasket.mobileapp.adapter.db.AppDataDynamicDbHelper;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;


public class GetAppDataDynamicIntentService extends IntentService {
    private static final String TAG = "GetAppDynamicService";

    public GetAppDataDynamicIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = MobileApiUrl.getMobileApiUrl(this) + "get-app-data-dynamic/";
        Request request = new Request.Builder()
                .url(url)
                .method("POST", RequestBody.create(null, new byte[0]))
                .build();
        OkHttpClient client = BigBasketApiAdapter.getHttpClient(this);
        String responseJson;
        try {
            com.squareup.okhttp.Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                responseJson = response.body().string();
            } else {
                responseJson = "";
            }
        } catch (IOException e) {
            responseJson = "";
        }
        AppDataDynamicDbHelper appDataDynamicDbHelper = new AppDataDynamicDbHelper(this);
        appDataDynamicDbHelper.save(responseJson);
    }
}
