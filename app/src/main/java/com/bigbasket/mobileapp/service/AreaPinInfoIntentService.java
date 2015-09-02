package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetAreaInfoResponse;
import com.bigbasket.mobileapp.model.CityManager;
import com.bigbasket.mobileapp.model.account.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;


/**
 * An {@link IntentService} subclass for refreshing area-pin codes.
 */
public class AreaPinInfoIntentService extends IntentService {

    public AreaPinInfoIntentService() {
        super("AreaPinInfoIntentService");
    }

    private static final String TAG = "AreaPinInfoService";

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        try {
            ArrayList<City> cities = bigBasketApiService.listCitySynchronously();
            CityManager.storeCities(AreaPinInfoIntentService.this, cities);
            fetchPinCodes(cities);
        } catch (RetrofitError e) {
            Log.d(TAG, "Oops! An error occurred while fetching pin-codes");
        }
    }

    public void fetchPinCodes(ArrayList<City> cities) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        boolean success = true;

        HashMap<City, HashMap<String, ArrayList<String>>> downloadedDataMap = new HashMap<>();
        for (City city : cities) {
            try {
                ApiResponse<GetAreaInfoResponse> response = bigBasketApiService.getAreaInfo(String.valueOf(city.getId()));
                if (response.status == 0) {
                    downloadedDataMap.put(city, response.apiResponseContent.pinCodeMaps);
                }
            } catch (RetrofitError r) {
                success = false;
                Log.d(TAG, "Oops! An error occurred while fetching pin-codes");
                break;
            }
        }
        if (success && downloadedDataMap.size() > 0) {
            AreaPinInfoAdapter areaPinInfoAdapter = new AreaPinInfoAdapter(this);
            DatabaseHelper.db.beginTransaction();
            areaPinInfoAdapter.deleteData();

            for (Map.Entry<City, HashMap<String, ArrayList<String>>> pinCodeEntrySet :
                    downloadedDataMap.entrySet()) {
                insertPinCodes(areaPinInfoAdapter,
                        pinCodeEntrySet.getKey(), pinCodeEntrySet.getValue());
            }

            DatabaseHelper.db.setTransactionSuccessful();
            DatabaseHelper.db.endTransaction();
            Log.d(TAG, "Successfully fetched all pin-codes");
            CityManager.setAreaPinInfoDate(this);
        }
    }

    private void insertPinCodes(AreaPinInfoAdapter areaPinInfoAdapter,
                                City city, HashMap<String, ArrayList<String>> pinCodeMaps) {
        for (Map.Entry<String, ArrayList<String>> pincodeAreaEntrySet : pinCodeMaps.entrySet()) {
            String pinCode = pincodeAreaEntrySet.getKey();
            ArrayList<String> areas = pincodeAreaEntrySet.getValue();
            for (String areaName : areas) {
                areaPinInfoAdapter.insert(areaName, pinCode, city.getName(), city.getId());
            }
        }
    }
}
