package com.bigbasket.mobileapp.apiservice;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

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

    public static void refreshBigBasketApiService(final Context context) {
        final AuthParameters authParameters = AuthParameters.getInstance(context);

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {

                String appVersion = UIUtil.getAppVersion(context);
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
                boolean isFirstTimeVisitor = AuthParameters.isFirstTimeVisitor(context);
                if (isFirstTimeVisitor) {
                    if (!TextUtils.isEmpty(requestCookieVal)) {
                        requestCookieVal += ";";
                    } else {
                        requestCookieVal = "";
                    }
                    requestCookieVal += "_bb_ftvid=\"true\"";
                }
                if (!TextUtils.isEmpty(requestCookieVal)) {
                    request.addHeader("Cookie", requestCookieVal);
                }
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(45, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(20, TimeUnit.SECONDS);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(MobileApiUrl.URL)
                .setRequestInterceptor(requestInterceptor)
                .setClient(new OkClient(okHttpClient))
                .build();

        //.setClient(new BigBasketHttpClient())
        bigBasketApiService = restAdapter.create(BigBasketApiService.class);
    }

//    public static final class BigBasketHttpClient extends UrlConnectionClient {
//        @Override
//        protected HttpURLConnection openConnection(Request request) throws IOException {
//            HttpURLConnection httpURLConnection = super.openConnection(request);
//            httpURLConnection.setConnectTimeout(20 * 1000);
//            httpURLConnection.setReadTimeout(45 * 1000);
//            return httpURLConnection;
//        }
//    }
}
