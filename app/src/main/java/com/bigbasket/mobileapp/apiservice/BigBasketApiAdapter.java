package com.bigbasket.mobileapp.apiservice;

import android.content.Context;

import com.bigbasket.mobileapp.devconfig.DeveloperConfigs;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;

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

    private static BigBasketApiService refreshBigBasketApiService(Context context) {

        OkHttpClient okHttpClient = getHttpClient(context);

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(MobileApiUrl.getMobileApiUrl(context))
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        return restAdapter.create(BigBasketApiService.class);
    }

    public static OkHttpClient getHttpClient(Context context) {
        final AuthParameters authParameters = AuthParameters.getInstance(context);
        OkHttpClient okHttpClient = getBaseHttpClient();
        okHttpClient.interceptors().add(new BigBasketApiInterceptor(authParameters.getVisitorId(),
                DataUtil.getAppVersion(context), authParameters.getBbAuthToken()));

        Interceptor loggingInterceptor = DeveloperConfigs.getHttpLoggingInterceptor(context);

        if (loggingInterceptor != null) {
            okHttpClient.interceptors().add(loggingInterceptor);
            okHttpClient.networkInterceptors().add(loggingInterceptor);
        }
        return okHttpClient;
    }

    public static OkHttpClient getBaseHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(20, TimeUnit.SECONDS);
        return okHttpClient;
    }
}
