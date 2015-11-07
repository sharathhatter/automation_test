package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetAppDataDynamicResponse;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.util.Constants;

import java.io.IOException;

import retrofit.Call;
import retrofit.Response;


public class GetAppDataDynamicIntentService extends IntentService {
    private static final String TAG = "GetAppDynamicService";

    public GetAppDataDynamicIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        try {
            Call<ApiResponse<GetAppDataDynamicResponse>> call = bigBasketApiService.getAppDataDynamic();
            Response<ApiResponse<GetAppDataDynamicResponse>> response = call.execute();
            if (response.isSuccess()) {
                ApiResponse<GetAppDataDynamicResponse> getDynamicPageApiResponse = response.body();
                if (getDynamicPageApiResponse.status == 0) {
                    AppDataDynamic.updateInstance(this,
                            getDynamicPageApiResponse.apiResponseContent.addToBasketPostParams,
                            getDynamicPageApiResponse.apiResponseContent.addressSummaries,
                            getDynamicPageApiResponse.apiResponseContent.isContextualMode,
                            getDynamicPageApiResponse.apiResponseContent.expressAvailability,
                            getDynamicPageApiResponse.apiResponseContent.abModeName,
                            getDynamicPageApiResponse.apiResponseContent.storeAvailabilityMap);
                    sendSuccessBroadcast();
                } else {
                    sendErrorBroadcast();
                }
            } else {
                sendErrorBroadcast();
            }
        } catch (IOException e) {
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
