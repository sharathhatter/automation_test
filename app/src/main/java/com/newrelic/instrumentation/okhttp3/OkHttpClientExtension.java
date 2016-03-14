package com.newrelic.instrumentation.okhttp3;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by muniraju on 01/03/16.
 */
public class OkHttpClientExtension extends OkHttpClient {
    private OkHttpClient impl;

    public OkHttpClientExtension(OkHttpClient okHttpClient) {
        this.impl = okHttpClient;
    }

    @Override
    public Call newCall(Request request) {
        return new CallExtension(this.impl, request, this.impl.newCall(request));
    }
}
