package com.bigbasket.mobileapp.model.request;

import android.support.annotation.Nullable;

import org.apache.http.client.CookieStore;

import java.util.HashMap;

public class HttpRequestData {
    private String url;
    private HashMap<String, String> params;
    private String bbAuthToken;
    private String bbVid;
    private String osVersion;
    CookieStore cookieStore;
    private boolean post;
    private HashMap<Object, String> additionalCtx;

    public HttpRequestData(String url, HashMap<String, String> params,
                           boolean post, @Nullable String bbAuthToken,
                           @Nullable String bbVid,
                           @Nullable String osVersion, CookieStore cookieStore,
                           @Nullable HashMap<Object, String> additionalCtx) {
        this.url = url;
        this.params = params;
        this.bbAuthToken = bbAuthToken;
        this.bbVid = bbVid;
        this.osVersion = osVersion;
        this.cookieStore = cookieStore;
        this.post = post;
        this.additionalCtx = additionalCtx;
    }

    public HashMap<Object, String> getAdditionalCtx() {
        return additionalCtx;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPost() {
        return post;
    }

    public String getBbAuthToken() {
        return bbAuthToken;
    }

    public String getBbVid() {
        return bbVid;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public String getUrl() {
        return url;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }
}
