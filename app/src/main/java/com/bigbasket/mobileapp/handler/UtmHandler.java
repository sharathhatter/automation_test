package com.bigbasket.mobileapp.handler;

import android.content.Context;
import android.net.Uri;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.util.DataUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import retrofit.RetrofitError;

public class UtmHandler {
    public static void postUtm(Context context, Uri uri) {
        if (!DataUtil.isInternetAvailable(context)) return;
        Set<String> queryParameterNames;
        try {
            queryParameterNames = getQueryParameterNames(uri);
        } catch (UnsupportedOperationException e) {
            return;
        }
        HashMap<String, String> utmQueryMap = null;
        if (queryParameterNames != null) {
            for (String queryParamName : queryParameterNames) {
                if (queryParamName != null && queryParamName.toLowerCase(Locale.getDefault()).startsWith("utm_")) {
                    if (utmQueryMap == null) {
                        utmQueryMap = new HashMap<>();
                    }
                    utmQueryMap.put(queryParamName, uri.getQueryParameter(queryParamName));
                }
            }
        }

        if (utmQueryMap == null || utmQueryMap.size() == 0) return;
        final HashMap<String, String> utmQueryMapHolder = utmQueryMap;
        final BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
        new Thread() {
            @Override
            public void run() {
                try {
                    bigBasketApiService.postUtmParams(utmQueryMapHolder);
                } catch (RetrofitError e) {

                }
            }
        }.start();
    }

    private static Set<String> getQueryParameterNames(Uri uri) {
        if (uri.isOpaque()) {
            throw new UnsupportedOperationException();
        }

        String query = uri.getEncodedQuery();
        if (query == null) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);
            names.add(Uri.decode(name));

            // Move start to end of name.
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableSet(names);
    }
}
