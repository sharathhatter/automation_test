package com.bigbasket.mobileapp.apiservice;

import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BigBasketApiInterceptor implements Interceptor {
    @Nullable
    private String visitorId;
    @Nullable
    private String bbAuthToken;
    private String appVersion;

    public BigBasketApiInterceptor(@Nullable String visitorId, String appVersion,
                                   @Nullable String bbAuthToken) {
        this.visitorId = visitorId;
        this.appVersion = appVersion;
        this.bbAuthToken = bbAuthToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String userAgentPrefix = "BB Android/" + "v" + appVersion + "/os ";
        String requestCookieVal = null;
        if (!TextUtils.isEmpty(visitorId)) {
            requestCookieVal = "_bb_vid=\"" + visitorId + "\"";
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
}
