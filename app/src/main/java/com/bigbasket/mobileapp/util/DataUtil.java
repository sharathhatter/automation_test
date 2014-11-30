package com.bigbasket.mobileapp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.bigbasket.mobileapp.util.Constants.CONNECTION_TIMEOUT;

public class DataUtil {

    private static final String TAG = DataUtil.class.getName();
    private static HashMap<String, String> addBasketKeyHash = new HashMap<String, String>() {{
//        put(Productdetail.class.getSimpleName(), "pd");
//        put(SearchFragment.class.getSimpleName(), "ps");
//        put(BrowseByOffersActivity.class.getSimpleName(), "of");
//        put(ShoppingList.class.getSimpleName(), "sl");
//        put(CategoryProductsActivity.class.getSimpleName(), "pc");
//        put(ShowCartActivity.class.getSimpleName(), "vb");
//        put(QuickShopActivity.class.getSimpleName(), "qs");
//        put(PromoSetProductsActivity.class.getSimpleName(), "pp");
        // TODO : Fix this functionality for add to basket
    }};

    public static boolean isInternetAvailable(Context context) {
        return getConnectionStatus(context) == MessageCode.NET_CONNECTED;
    }

    public static int getConnectionStatus(Context context) {
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            if (networkInfo.isConnected()) {
                return MessageCode.NET_CONNECTED;
            } else if (networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                return MessageCode.NET_CONNECTING;
            }
        }
        return MessageCode.NET_DISCONNECTED;
    }

    public static HttpOperationResult doHttpGet(HttpRequestData httpRequestData) {
        String getUrl = httpRequestData.getUrl() + (httpRequestData.getParams() !=
                null && httpRequestData.getParams().size() > 0 ?
                getUrlEncodedParams(httpRequestData.getParams()) : "");
        Log.d("Server call initiated URL =>", getUrl);
        HttpResponse response = null;
        HttpOperationResult result = null;

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, Constants.SOCKET_TIMEOUT);
        DefaultHttpClient client;
        try {
            client = new DefaultHttpClient(httpParams);
            client.setCookieStore(httpRequestData.getCookieStore());
            HttpGet httpGet = new HttpGet(getUrl);
            HttpContext localContext1 = new BasicHttpContext();
            localContext1.setAttribute(ClientContext.COOKIE_STORE,
                    httpRequestData.getCookieStore());
            String requestCookieVal = null;
            String bbVisitorId = httpRequestData.getBbVid();
            String bbAuthToken = httpRequestData.getBbAuthToken();
            String osVersion = httpRequestData.getOsVersion();
            if (TextUtils.isEmpty(osVersion)) {
                osVersion = Build.VERSION.RELEASE;
            }
            if (!TextUtils.isEmpty(bbVisitorId)) {
                requestCookieVal = "_bb_vid=\"" + bbVisitorId + "\"";
            }
            if (!TextUtils.isEmpty(bbAuthToken)) {
                if (!TextUtils.isEmpty(bbAuthToken)) {
                    requestCookieVal += ";";
                } else {
                    requestCookieVal = "";
                }
                requestCookieVal += "BBAUTHTOKEN=\"" + bbAuthToken + "\"";
            }
            if (!TextUtils.isEmpty(requestCookieVal)) {
                httpGet.setHeader("Cookie", requestCookieVal);
            }
            httpGet.setHeader("User-Agent", Constants.USER_AGENT_PREFIX + osVersion);
            client.setCookieStore(httpRequestData.getCookieStore());
            response = client.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            Log.d(TAG, "Response Code: " + responseCode);
            if (responseCode == HttpCode.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(response.getEntity().getContent(), HTTP.UTF_8));
                String strResultJson = reader.readLine();
                Log.d(TAG, "Response Data: " + strResultJson);
                result = new HttpOperationResult(strResultJson, responseCode,
                        httpRequestData.getUrl(), false);
                if (httpRequestData.getAdditionalCtx() != null) {
                    result.setAdditionalCtx(httpRequestData.getAdditionalCtx());
                }
            }
            Log.d("Server response for URL =>", getUrl);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return result;
    }

    public static HttpOperationResult doHttpPost(HttpRequestData httpRequestData) {
        String postUrl = httpRequestData.getUrl();
        HttpOperationResult result = null;
        Log.d("Server call initiated URL =>", httpRequestData.getUrl());
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, Constants.SOCKET_TIMEOUT);
        DefaultHttpClient client;
        CookieStore cookieStore = new BasicCookieStore();
        String bbVisitorId = httpRequestData.getBbVid();
        String bbAuthToken = httpRequestData.getBbAuthToken();
        String osVersion = httpRequestData.getOsVersion();
        if (TextUtils.isEmpty(osVersion)) {
            osVersion = Build.VERSION.RELEASE;
        }

        try {
            client = new DefaultHttpClient(httpParams);
            client.setCookieStore(cookieStore);
            HttpPost httpPost = new HttpPost(postUrl);
            String requestCookieVal = null;
            if (!TextUtils.isEmpty(bbVisitorId)) {
                requestCookieVal = "_bb_vid=\"" + bbVisitorId + "\"";
            }
            if (!TextUtils.isEmpty(bbAuthToken)) {
                if (!TextUtils.isEmpty(bbAuthToken)) {
                    requestCookieVal += ";";
                } else {
                    requestCookieVal = "";
                }
                requestCookieVal += "BBAUTHTOKEN=\"" + bbAuthToken + "\"";
            }
            if (!TextUtils.isEmpty(requestCookieVal)) {
                httpPost.setHeader("Cookie", requestCookieVal);
            }
            httpPost.setHeader("User-Agent", Constants.USER_AGENT_PREFIX + osVersion);
            if (httpRequestData.getParams() != null && httpRequestData.getParams().size() > 0) {
                try {
                    httpPost.setEntity(new
                            UrlEncodedFormEntity(getNameValuePairs(httpRequestData.getParams()),
                            HTTP.UTF_8));
                } catch (UnsupportedEncodingException ex) {
                    Log.e(TAG, ex.toString());
                    return null;
                }
            }
            client.setCookieStore(cookieStore);
            HttpResponse response = client.execute(httpPost);

            int responseCode = response.getStatusLine().getStatusCode();
            Log.d(TAG, "Response Code: " + responseCode);
            if (responseCode == HttpStatus.SC_OK || responseCode == HttpStatus.SC_ACCEPTED) {
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(response.getEntity().getContent(), HTTP.UTF_8));
                String strResultJson = reader.readLine();
                result = new HttpOperationResult(strResultJson, responseCode, postUrl, true);
                if (httpRequestData.getAdditionalCtx() != null) {
                    result.setAdditionalCtx(httpRequestData.getAdditionalCtx());
                }
            }
            Log.d("Server response for URL =>", httpRequestData.getUrl());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return result;
    }

    public static HttpOperationResult doHttpGet(String url, CookieStore cookieStore, String bbVid, String bbAuthToken, String osVersion) {
        HttpResponse response = null;
        HttpOperationResult result = null;
        Log.d("Server call initiated URL =>", url);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, Constants.SOCKET_TIMEOUT);
        DefaultHttpClient client;
        try {
            client = new DefaultHttpClient(httpParams);
            client.setCookieStore(cookieStore);
            HttpGet httpGet = new HttpGet(url);
            HttpContext localContext1 = new BasicHttpContext();
            localContext1.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            if (bbAuthToken.length() == 0) {
                httpGet.setHeader("Cookie", "_bb_vid=" + bbVid);
            } else {
                httpGet.setHeader("Cookie", "_bb_vid=\"" + bbVid + "\";BBAUTHTOKEN=\"" + bbAuthToken + "\"");
            }
            httpGet.setHeader("User-Agent", Constants.USER_AGENT_PREFIX + osVersion);
            client.setCookieStore(cookieStore);
            response = client.execute(httpGet);

            int responseCode = response.getStatusLine().getStatusCode();
            JSONObject jsonObject = null;
            Log.d(TAG, "Response Code: " + responseCode);
            if (responseCode == HttpCode.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String strResultJson = reader.readLine();
                jsonObject = new JSONObject(new JSONTokener(strResultJson));
                Log.d(TAG, "Response Data: " + jsonObject.toString());
            }
            Log.d("Server response for URL =>", url);
            result = new HttpOperationResult(jsonObject, responseCode);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return result;

    }

    public static HttpOperationResult doHttpGet(Context context, String url) {
        HttpOperationResult result = null;
        Log.d("Server request for URL =>", url);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, Constants.SOCKET_TIMEOUT);
        DefaultHttpClient client;

        CookieStore cookieStore = new BasicCookieStore();
        String bbVisitorId = AuthParameters.getInstance(context).getVisitorId();
        String bbAuthToken = AuthParameters.getInstance(context).getBbAuthToken();
        String osVersion = AuthParameters.getInstance(context).getOsVersion();

        try {
            client = new DefaultHttpClient(httpParams);
            client.setCookieStore(cookieStore);
            HttpGet httpGet = new HttpGet(url);
            HttpContext localContext1 = new BasicHttpContext();
            localContext1.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            if (bbAuthToken.length() == 0) {
                httpGet.setHeader("Cookie", "_bb_vid=" + bbVisitorId);
            } else {
                httpGet.setHeader("Cookie", "_bb_vid=\"" + bbVisitorId + "\";BBAUTHTOKEN=\"" + bbAuthToken + "\"");
            }
            httpGet.setHeader("User-Agent", Constants.USER_AGENT_PREFIX + osVersion);
            client.setCookieStore(cookieStore);
            HttpResponse response = client.execute(httpGet);

            int responseCode = response.getStatusLine().getStatusCode();
            JSONObject jsonObject = null;
            Log.d(TAG, "Response Code: " + responseCode);
            if (responseCode == HttpCode.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String strResultJson = reader.readLine();
                jsonObject = new JSONObject(new JSONTokener(strResultJson));
                Log.d(TAG, "Response Data: " + jsonObject.toString());
            }
            Log.d("Server response for URL =>", url);
            result = new HttpOperationResult(jsonObject, responseCode);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return result;

    }

    public static String getUrlEncodedParams(HashMap<String, String> requestParams) {
        String paramString = "?";
        List<NameValuePair> params = getNameValuePairs(requestParams);
        paramString += URLEncodedUtils.format(params, "utf-8");
        return paramString;
    }

    private static List<NameValuePair> getNameValuePairs(HashMap<String, String> requestParams) {
        List<NameValuePair> params = new LinkedList<>();
        for (String paramName : requestParams.keySet()) {
            params.add(new BasicNameValuePair(paramName, requestParams.get(paramName)));
        }
        return params;
    }

    public static String getAddBasketNavigationActivity(String activityName) {
        return addBasketKeyHash.get(activityName);
    }
}