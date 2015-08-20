package com.bigbasket.mobileapp.task.uiv3;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackGetAreaInfo;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.CityManager;

public class GetAreaPinInfoTask<T> {
    private T ctx;

    public GetAreaPinInfoTask(T ctx) {
        this.ctx = ctx;
    }

    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError(true);
            return;
        }
        if (CityManager.isAreaPinInfoDataValidStale(((ActivityAware) ctx).getCurrentActivity())) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter
                    .getApiService(((ActivityAware) ctx).getCurrentActivity());
            ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
            bigBasketApiService.getAreaInfo(new CallbackGetAreaInfo<>(ctx));
        } else {
            ((PinCodeAware) ctx).onPinCodeFetchSuccess();
        }
    }
}
