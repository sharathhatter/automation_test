package com.bigbasket.mobileapp.apiservice;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class BigBasketApiAdapter {

    private static BigBasketApiService bigBasketApiService;

    private BigBasketApiAdapter() {
    }

    public static BigBasketApiService getApiService(Context context) {
        if (bigBasketApiService == null) {
            refreshBigBasketApiService(context);
        }
        return bigBasketApiService;
    }

    public static void refreshBigBasketApiService(final Context context) {
        final AuthParameters authParameters = AuthParameters.getInstance(context);

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {

                String appVersion = DataUtil.getAppVersion(context);
                String userAgentPrefix = "BB Android/" + "v" + appVersion + "/os ";
                request.addHeader("User-Agent", userAgentPrefix + Build.VERSION.RELEASE);

                String bbVisitorId = authParameters.getVisitorId();
                String bbAuthToken = authParameters.getBbAuthToken();
                String requestCookieVal = null;
                if (!TextUtils.isEmpty(bbVisitorId)) {
                    requestCookieVal = "_bb_vid=\"" + bbVisitorId + "\"";
                }
                if (!TextUtils.isEmpty(bbAuthToken)) {
                    if (!TextUtils.isEmpty(requestCookieVal)) {
                        requestCookieVal += ";";
                    } else {
                        requestCookieVal = "";
                    }
                    requestCookieVal += "BBAUTHTOKEN=\"" + bbAuthToken + "\"";
                }
                if (!TextUtils.isEmpty(requestCookieVal)) {
                    request.addHeader("Cookie", requestCookieVal);
                }
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(20, TimeUnit.SECONDS);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(MobileApiUrl.URL)
                .setRequestInterceptor(requestInterceptor)
                .setClient(new OkClient(okHttpClient))
                .build();

        bigBasketApiService = restAdapter.create(BigBasketApiService.class);
    }

}
