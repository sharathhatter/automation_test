package com.bigbasket.mobileapp.apiservice;

import android.content.Context;

import com.bigbasket.mobileapp.devconfig.DeveloperConfigs;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BigBasketApiAdapter {

    private static volatile BigBasketApiService bigBasketApiService;
    private static volatile OkHttpClient okHttpClient;

    private BigBasketApiAdapter() {
    }

    public static synchronized BigBasketApiService getApiService(Context context) {
        if (bigBasketApiService == null) {
            bigBasketApiService = refreshBigBasketApiService(context);
        }
        return bigBasketApiService;
    }

    public static synchronized void reset() {
        bigBasketApiService = null;
        okHttpClient = null;
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
        if (okHttpClient != null) return okHttpClient;
        final AuthParameters authParameters = AuthParameters.getInstance(context);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .addInterceptor(new BigBasketApiInterceptor(authParameters.getVisitorId(),
                        DataUtil.getAppVersion(context), authParameters.getBbAuthToken()))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);

        Interceptor loggingInterceptor = DeveloperConfigs.getHttpLoggingInterceptor(context);

        if (loggingInterceptor != null) {
            builder.addInterceptor(loggingInterceptor);
        }
        okHttpClient = builder.build();
        return okHttpClient;
    }

    public static OkHttpClient getBaseHttpClient() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
