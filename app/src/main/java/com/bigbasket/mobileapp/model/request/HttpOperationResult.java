package com.bigbasket.mobileapp.model.request;

import org.json.JSONObject;

import java.util.HashMap;

public class HttpOperationResult {
    private String reponseString;
    private JSONObject jsonObject;
    private int responseCode;
    private String url;
    private boolean post;
    private HashMap<Object, String> additionalCtx;


    public HttpOperationResult(String reponseString, int responseCode, String url, boolean post) {
        this.reponseString = reponseString;
        this.responseCode = responseCode;
        this.url = url;
        this.post = post;
    }

    public HttpOperationResult(String reponseString, int responseCode,
                               String url, boolean post, HashMap<Object, String> additionalCtx) {
        this.reponseString = reponseString;
        this.responseCode = responseCode;
        this.url = url;
        this.post = post;
        this.additionalCtx = additionalCtx;
    }

    public HttpOperationResult(JSONObject jsonObject, int responseCode) {
        this.jsonObject = jsonObject;
        this.responseCode = responseCode;
    }

    public HashMap<Object, String> getAdditionalCtx() {
        return additionalCtx;
    }

    public void setAdditionalCtx(HashMap<Object, String> additionalCtx) {
        this.additionalCtx = additionalCtx;
    }

    public String getReponseString() {
        return reponseString;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPost() {
        return post;
    }

    public void setPost(boolean post) {
        this.post = post;
    }

    public void setReponseString(String reponseString) {
        this.reponseString = reponseString;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
