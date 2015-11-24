package com.bigbasket.mobileapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoDbHelper;
import com.bigbasket.mobileapp.adapter.db.DatabaseHelper;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetAreaInfoResponse;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.account.City;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;


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
            Call<ArrayList<City>> call = bigBasketApiService.listCities();
            Response<ArrayList<City>> response = call.execute();
            if (response.isSuccess()) {
                ArrayList<City> cities = response.body();
                CityManager.storeCities(AreaPinInfoIntentService.this, cities);
                fetchPinCodes(cities);
            } else {
                Log.d(TAG, "Oops! An error occurred while fetching pin-codes");
            }
        } catch (IOException e) {
            Log.d(TAG, "Oops! An error occurred while fetching pin-codes");
        }
    }

    private void fetchPinCodes(ArrayList<City> cities) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        boolean success = true;

        HashMap<City, HashMap<String, ArrayList<String>>> downloadedDataMap = new HashMap<>();
        for (City city : cities) {
            try {
                Call<ApiResponse<GetAreaInfoResponse>> call = bigBasketApiService.getAreaInfo(String.valueOf(city.getId()));
                Response<ApiResponse<GetAreaInfoResponse>> apiResponse = call.execute();
                if (apiResponse.isSuccess()) {
                    ApiResponse<GetAreaInfoResponse> response = apiResponse.body();
                    if (response.status == 0) {
                        downloadedDataMap.put(city, response.apiResponseContent.pinCodeMaps);
                    }
                } else {
                    success = false;
                    Log.d(TAG, "Oops! An error occurred while fetching pin-codes");
                }
            } catch (IOException r) {
                success = false;
                Log.d(TAG, "Oops! An error occurred while fetching pin-codes");
                break;
            }
        }
        if (success && downloadedDataMap.size() > 0) {
            AreaPinInfoDbHelper areaPinInfoDbHelper = new AreaPinInfoDbHelper(this);
            DatabaseHelper.db.beginTransaction();
            areaPinInfoDbHelper.deleteData();

            for (Map.Entry<City, HashMap<String, ArrayList<String>>> pinCodeEntrySet :
                    downloadedDataMap.entrySet()) {
                insertPinCodes(areaPinInfoDbHelper,
                        pinCodeEntrySet.getKey(), pinCodeEntrySet.getValue());
            }

            DatabaseHelper.db.setTransactionSuccessful();
            DatabaseHelper.db.endTransaction();
            Log.d(TAG, "Successfully fetched all pin-codes");
            CityManager.setAreaPinInfoDate(this);
        }
    }

    private void insertPinCodes(AreaPinInfoDbHelper areaPinInfoDbHelper,
                                City city, HashMap<String, ArrayList<String>> pinCodeMaps) {
        for (Map.Entry<String, ArrayList<String>> pincodeAreaEntrySet : pinCodeMaps.entrySet()) {
            String pinCode = pincodeAreaEntrySet.getKey();
            ArrayList<String> areas = pincodeAreaEntrySet.getValue();
            for (String areaName : areas) {
                areaPinInfoDbHelper.insert(areaName, pinCode, city.getName(), city.getId());
            }
        }
    }
}
