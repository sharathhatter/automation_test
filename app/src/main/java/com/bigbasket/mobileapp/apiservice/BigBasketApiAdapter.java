package com.bigbasket.mobileapp.apiservice;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class BigBasketApiAdapter {

    private static volatile BigBasketApiService bigBasketApiService;
    private static final Object lock = new Object();

    private BigBasketApiAdapter() {
    }

    public static BigBasketApiService getApiService(Context context) {
        BigBasketApiService localInstance = bigBasketApiService;
        if (localInstance == null) {
            synchronized (lock) {
                localInstance = bigBasketApiService;
                if (localInstance == null) {
                    localInstance = refreshBigBasketApiService(context);
                    bigBasketApiService = localInstance;
                }
            }
        }
        return bigBasketApiService;
    }

    public static void reset() {
        synchronized (lock) {
            bigBasketApiService = null;
        }
    }

    private static BigBasketApiService refreshBigBasketApiService(final Context context) {
        final AuthParameters authParameters = AuthParameters.getInstance(context);

        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                String appVersion = DataUtil.getAppVersion(context);
                String userAgentPrefix = "BB Android/" + "v" + appVersion + "/os ";
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

                Request.Builder newRequestBuilder = originalRequest.newBuilder()
                        .removeHeader("User-Agent");
                newRequestBuilder.addHeader("User-Agent", userAgentPrefix + Build.VERSION.RELEASE);
                if (!TextUtils.isEmpty(requestCookieVal)) {
                    newRequestBuilder.addHeader("Cookie", requestCookieVal);
                }
                return chain.proceed(newRequestBuilder.build());
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(20, TimeUnit.SECONDS);
        okHttpClient.interceptors().add(interceptor);

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(MobileApiUrl.getMobileApiUrl(context))
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        return restAdapter.create(BigBasketApiService.class);
    }
}
