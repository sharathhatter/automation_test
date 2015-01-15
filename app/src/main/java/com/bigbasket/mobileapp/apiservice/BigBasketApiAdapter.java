package com.bigbasket.mobileapp.apiservice;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Request;
import retrofit.client.UrlConnectionClient;

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
                if (!TextUtils.isEmpty(requestCookieVal)) {
                    request.addHeader("Cookie", requestCookieVal);
                }
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(MobileApiUrl.URL)
                .setRequestInterceptor(requestInterceptor)
                .setClient(new BigBasketHttpUrlConnection())
                .build();

        bigBasketApiService = restAdapter.create(BigBasketApiService.class);
    }

    public static final class BigBasketHttpUrlConnection extends UrlConnectionClient {
        @Override
        protected HttpURLConnection openConnection(Request request) throws IOException {
            HttpURLConnection connection = super.openConnection(request);
            connection.setConnectTimeout(20 * 1000);
            connection.setReadTimeout(45 * 1000);
            return connection;
        }
    }
}
