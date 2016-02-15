package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.account.City;

import java.util.ArrayList;

import retrofit2.Call;

public class GetCitiesTask<T extends CityListDisplayAware & AppOperationAware> {
    private T context;

    public GetCitiesTask(T context) {
        this.context = context;
    }

    public void startTask() {
        ArrayList<City> cities = CityManager.getStoredCity(context.getCurrentActivity());
        if (cities != null && cities.size() > 0) {
            context.onReadyToDisplayCity(cities);
            return;
        }
        if (!context.checkInternetConnection()) {
            context.getHandler().sendOfflineError(true);
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context.getCurrentActivity());
        context.showProgressDialog(context.getCurrentActivity().getString(R.string.please_wait));
        Call<ArrayList<City>> call = bigBasketApiService.listCities();
        call.enqueue(new BBNetworkCallback<ArrayList<City>>(context, true) {
            @Override
            public void onSuccess(ArrayList<City> cities) {
                CityManager.storeCities(context.getCurrentActivity(), cities);
                context.onReadyToDisplayCity(cities);
            }

            @Override
            public boolean updateProgress() {
                try {
                    context.hideProgressDialog();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });
    }
}
