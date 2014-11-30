package com.bigbasket.mobileapp.apiservice;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class BigBasketApiAdapter {

    private BigBasketApiAdapter() {
    }

    private static BigBasketApiService bigBasketApiService;

    public static BigBasketApiService getApiService(Context context) {
        if (bigBasketApiService == null) {
            refreshBigBasketApiService(context);
        }
        return bigBasketApiService;
    }

    public static void refreshBigBasketApiService(Context context) {
        final AuthParameters authParameters = AuthParameters.getInstance(context);

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("User-Agent", Constants.USER_AGENT_PREFIX + Build.VERSION.RELEASE);

                String bbVisitorId = authParameters.getVisitorId();
                String bbAuthToken = authParameters.getBbAuthToken();
                String requestCookieVal = null;
                if (!TextUtils.isEmpty(bbVisitorId)) {
                    requestCookieVal = "_bb_vid=\"" + bbVisitorId + "\"";
                }
                if (!TextUtils.isEmpty(bbAuthToken)) {
                    if (!TextUtils.isEmpty(bbAuthToken)) {
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

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://dev1.bigbasket.com/mapi/v2.0.0")
                .setRequestInterceptor(requestInterceptor)
                .build();

        bigBasketApiService = restAdapter.create(BigBasketApiService.class);
    }
}
