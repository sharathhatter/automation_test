package com.bigbasket.mobileapp.handler;

import android.content.Context;
import android.net.Uri;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.util.DataUtil;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UtmHandler {
    public static void postUtm(Context context, Uri uri) {
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
        Tracker t = BaseApplication.getDefaultGATracker(context);
        // Campaign data sent with this hit.
        t.send(new HitBuilders.ScreenViewBuilder()
                .setCampaignParamsFromUrl(uri.toString())
                .build());
        BaseApplication.getDefaultGATracker(context).setCampaignParamsOnNextHit(uri);
        final HashMap<String, String> utmQueryMapHolder = utmQueryMap;
        final BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
        if (!DataUtil.isInternetAvailable(context)) return;
        //TODO: Send these utm params later when network is available
        Call<BaseApiResponse> call = bigBasketApiService.postUtmParams(utmQueryMapHolder);
        call.enqueue(new Callback<BaseApiResponse>() {
            @Override
            public void onResponse(Call<BaseApiResponse> call, Response<BaseApiResponse> response) {
                //Ignore
            }

            @Override
            public void onFailure(Call<BaseApiResponse> call, Throwable t) {
                //Ignore
            }
        });
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
