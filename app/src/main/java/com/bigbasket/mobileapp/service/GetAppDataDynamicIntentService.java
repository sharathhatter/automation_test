package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetAppDataDynamicResponse;
import com.bigbasket.mobileapp.managers.AddressManager;
import com.bigbasket.mobileapp.util.Constants;

import retrofit.RetrofitError;

public class GetAppDataDynamicIntentService extends IntentService {
    private static final String TAG = "GetAppDynamicService";

    public GetAppDataDynamicIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        try {
            ApiResponse<GetAppDataDynamicResponse> getDynamicPageApiResponse = bigBasketApiService.getAppDataDynamic();
            if (getDynamicPageApiResponse.status == 0
                    && getDynamicPageApiResponse.apiResponseContent.addressSummaries != null
                    && getDynamicPageApiResponse.apiResponseContent.addressSummaries.size() > 0) {
                AddressManager.storeAddresses(this, getDynamicPageApiResponse.apiResponseContent.addressSummaries);
                sendSuccessBroadcast();
            } else {
                sendErrorBroadcast();
            }
        } catch (RetrofitError e) {
            sendErrorBroadcast();
        }
    }

    private void sendSuccessBroadcast() {
        Intent addressChangedIntent = new Intent(Constants.ADDRESS_SYNC_BROADCAST_ACTION)
                .putExtra(Constants.STATUS, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(addressChangedIntent);
    }

    private void sendErrorBroadcast() {
        Intent addressChangedIntent = new Intent(Constants.ADDRESS_SYNC_BROADCAST_ACTION)
                .putExtra(Constants.STATUS, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(addressChangedIntent);
    }
}
