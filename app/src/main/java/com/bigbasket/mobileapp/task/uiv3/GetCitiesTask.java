package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.account.City;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GetCitiesTask<T> {
    private T context;

    public GetCitiesTask(T context) {
        this.context = context;
    }

    public void startTask() {
        ArrayList<City> cities = CityManager.getStoredCity(((ActivityAware) context).getCurrentActivity());
        if (cities != null && cities.size() > 0) {
            ((CityListDisplayAware) context).onReadyToDisplayCity(cities);
            return;
        }
        if (!((ConnectivityAware) context).checkInternetConnection()) {
            ((HandlerAware) context).getHandler().sendOfflineError(true);
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) context).getCurrentActivity());
        ((ProgressIndicationAware) context).showProgressDialog(((ActivityAware) context).getCurrentActivity().getString(R.string.please_wait));
        bigBasketApiService.listCities(new Callback<ArrayList<City>>() {
            @Override
            public void success(ArrayList<City> cities, Response response) {
                if (((CancelableAware) context).isSuspended()) {
                    return;
                }
                try {
                    ((ProgressIndicationAware) context).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                CityManager.storeCities(((ActivityAware) context).getCurrentActivity(), cities);
                ((CityListDisplayAware) context).onReadyToDisplayCity(cities);
            }

            @Override
            public void failure(RetrofitError error) {
                if (((CancelableAware) context).isSuspended()) {
                    return;
                }
                try {
                    ((ProgressIndicationAware) context).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                ((HandlerAware) context).getHandler().handleRetrofitError(error, true);
            }
        });
    }
}
