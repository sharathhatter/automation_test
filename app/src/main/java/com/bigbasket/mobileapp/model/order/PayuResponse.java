package com.bigbasket.mobileapp.model.order;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bigbasket.mobileapp.util.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class PayuResponse {
    private String amount;
    private String pid;
    private String failureReason;
    private String txnId;
    private boolean isSuccess;
    private static PayuResponse payuResponse;

    public static void createInstance(Context ctx,
                                      String capturedUrl, boolean isSuccess,
                                      String pid) {
        try {
            payuResponse = new PayuResponse(capturedUrl, isSuccess, pid);
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(ctx);
            if (prefer != null) {
                saveTxnDetailToPreference(ctx, capturedUrl, isSuccess);
            }
        } catch (URISyntaxException e) {
            payuResponse = null;
        }
    }

    public static PayuResponse getInstance(Context ctx) {
        if (payuResponse == null) {
            try {
                setUpInstanceFromPreference(ctx);
            } catch (URISyntaxException e) {
                return null;
            }
        }
        return payuResponse;
    }

    private static void saveTxnDetailToPreference(Context ctx, String capturedUrl, boolean isSuccess) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putString(Constants.CAPTURED_URL_KEY, capturedUrl);
        editor.putBoolean(Constants.IS_PAYU_SUCCESS_KEY, isSuccess);
        editor.commit();
    }

    public static void clearTxnDetail(Context ctx) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefer.edit();
        editor.remove(Constants.CAPTURED_URL_KEY);
        editor.remove(Constants.IS_PAYU_SUCCESS_KEY);
        editor.commit();
        payuResponse = null;
    }

    private static void setUpInstanceFromPreference(Context ctx)
            throws URISyntaxException {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(ctx);
        String capturedUrl = prefer.getString(Constants.CAPTURED_URL_KEY, null);
        String pid = prefer.getString(Constants.POTENTIAL_ORDER_ID, null);
        if (capturedUrl != null && pid != null) {
            boolean isSuccess = prefer.getBoolean(Constants.IS_PAYU_SUCCESS_KEY, false);
            createInstance(ctx, capturedUrl, isSuccess, pid);
        }
    }

    private PayuResponse(String capturedUrl, boolean isSuccess, String pid)
            throws URISyntaxException {
        this.isSuccess = isSuccess;
        this.pid = pid;
        if (isSuccess) {
            parseSuccessUrl(capturedUrl);
        } else {
            parseErrorUrl(capturedUrl);
        }
    }

    private void parseSuccessUrl(String capturedUrl)
            throws URISyntaxException {
        URI uri = new URI(capturedUrl);
        String queryStr = uri.getQuery();
        Map<String, String> queryMap = getQueryMap(queryStr);
        for (Map.Entry<String, String> query : queryMap.entrySet()) {
            switch (query.getKey()) {
                case Constants.AMOUNT:
                    this.amount = query.getValue();
                    break;
                case Constants.TXN_ID:
                    this.txnId = query.getValue();
            }
        }
    }

    private void parseErrorUrl(String capturedUrl)
            throws URISyntaxException {
        URI uri = new URI(capturedUrl);
        String queryStr = uri.getQuery();
        Map<String, String> queryMap = getQueryMap(queryStr);
        if (queryMap == null)
            return;
        for (Map.Entry<String, String> query : queryMap.entrySet()) {
            switch (query.getKey()) {
                case Constants.MSG:
                    this.failureReason = query.getValue();
                    break;
            }
        }
    }

    private Map<String, String> getQueryMap(String queryStr) {
        Map<String, String> queryMap = null;
        if (queryStr != null) {
            queryMap = new HashMap<>();
            for (String queryKeyValPair : queryStr.split("&")) {
                if (queryKeyValPair.contains("=")) {
                    String[] splittedParams = queryKeyValPair.split("=");
                    if (splittedParams.length != 2)
                        continue;
                    try {
                        queryMap.put(splittedParams[0], URLDecoder.decode(splittedParams[1], "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Payu Response Parser ", "Unable to url-decode " + splittedParams[1]);
                    }
                } else {
                    queryMap.put(queryKeyValPair, "");
                }
            }
        }
        return queryMap;
    }

    public String getAmount() {
        return amount;
    }

    public String getPid() {
        return pid;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getTxnId() {
        return txnId;
    }

    @Override
    public String toString() {
        return "PayuResponse{" +
                "amount='" + amount + '\'' +
                ", pid='" + pid + '\'' +
                ", failureReason='" + failureReason + '\'' +
                ", txnId='" + txnId + '\'' +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
