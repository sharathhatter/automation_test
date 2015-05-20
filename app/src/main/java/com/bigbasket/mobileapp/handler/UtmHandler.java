package com.bigbasket.mobileapp.handler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.util.DataUtil;

import retrofit.RetrofitError;

public class UtmHandler {
    public static void postUtm(Context context,
                               @Nullable final String source, @Nullable final String medium,
                               @Nullable final String term, @Nullable final String content,
                               @Nullable final String campaign) {
        if (!DataUtil.isInternetAvailable(context)) return;
        if (!TextUtils.isEmpty(source) || !TextUtils.isEmpty(medium) ||
                !TextUtils.isEmpty(term) || !TextUtils.isEmpty(content) ||
                !TextUtils.isEmpty(campaign)) {
            final BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
            new Thread() {
                @Override
                public void run() {
                    try {
                        bigBasketApiService.postUtmParams(source, medium, term, content, campaign);
                    } catch (RetrofitError e) {

                    }
                }
            }.start();
        }
    }
}
