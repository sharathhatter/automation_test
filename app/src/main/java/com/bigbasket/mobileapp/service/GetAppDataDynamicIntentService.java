package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.bigbasket.mobileapp.adapter.db.AppDataDynamicDbHelper;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.contentProvider.SectionItemAnalyticsData;
import com.bigbasket.mobileapp.model.ads.AdAnalyticsData;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;


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
        //Upload pending analytics data
        if(DataUtil.isInternetAvailable(this)){
            List<SectionItemAnalyticsData> data = AnalyticsIntentService.getAnalyticsData(this);
            if(data == null || data.isEmpty()){
                return;
            }
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            AdAnalyticsData[] analyticsData = new AdAnalyticsData[data.size()];
            int i = 0;
            for(SectionItemAnalyticsData sd: data) {
                AdAnalyticsData aad = new AdAnalyticsData();
                aad.setClicks(sd.getClicks());
                aad.setImps(sd.getImpressions());
                int cityId = 1;
                try {
                    cityId = Integer.parseInt(sd.getCityId());
                } catch (NumberFormatException ex) {
                    //Should never happen
                    Crashlytics.logException(ex);
                }
                aad.setCityId(cityId);
                if(!TextUtils.isEmpty(sd.getAnalyticsAttrs())) {
                    HashMap<String, String> analyticsAttrMap =
                            gson.fromJson(sd.getAnalyticsAttrs(), type);
                    aad.setAnalyticsAttr(analyticsAttrMap);
                }
                analyticsData[i] = aad;
                i++;
            }
            BigBasketApiService bbApiService = BigBasketApiAdapter.getApiService(this);
            Call<BaseApiResponse> call = bbApiService.postAdAnalytics(analyticsData);
            Response<BaseApiResponse> response = null;
            try {
                response = call.execute();
            } catch (IOException e) {
                //Ignore, try again
            }

            if(response == null || !response.isSuccess() || response.body().status != 0) {
                // POST request failed, Restore the data back to the db, which will be retried later
                // Dont just insert here, there may have ben some clicks or impressions recorded
                // during the execution of above statements
                for(SectionItemAnalyticsData sd: data) {
                    AnalyticsIntentService.startUpdateSponsoredProductEvent(this,
                            sd.getClicks(),
                            sd.getImpressions(),
                            sd.getSectionId(),
                            sd.getCityId(),
                            sd.getAnalyticsAttrs());
                }
            }

        }
    }
}
