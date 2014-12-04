package com.bigbasket.mobileapp.apiservice.callbacks;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetAreaInfoResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;

import java.util.ArrayList;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CallbackGetAreaInfo<T> implements Callback<ApiResponse<GetAreaInfoResponse>> {

    private T ctx;

    public CallbackGetAreaInfo(T ctx) {
        this.ctx = ctx;
    }

    @Override
    public void success(ApiResponse<GetAreaInfoResponse> getAreaInfoResponseApiResponse, Response response) {
        if (((CancelableAware) ctx).isSuspended()) return;
        try {
            ((ProgressIndicationAware) ctx).hideProgressView();
        } catch (IllegalArgumentException e) {
            return;
        }
        switch (getAreaInfoResponseApiResponse.status) {
            case 0:
                if (getAreaInfoResponseApiResponse.apiResponseContent.pinCodeMaps != null) {
                    AreaPinInfoAdapter areaPinInfoAdapter = new AreaPinInfoAdapter(((ActivityAware) ctx).
                            getCurrentActivity());
                    for (Map.Entry<String, ArrayList<String>> pinCodeMapEntry :
                            getAreaInfoResponseApiResponse.apiResponseContent.pinCodeMaps.entrySet()) {
                        for (String areaName : pinCodeMapEntry.getValue()) {
                            areaPinInfoAdapter.insert(areaName.toLowerCase(), pinCodeMapEntry.getKey());
                        }
                    }
                    ((PinCodeAware) ctx).onPinCodeFetchSuccess();
                    return;
                }
                ((PinCodeAware) ctx).onPinCodeFetchFailure();
                break;
            default:
                ((PinCodeAware) ctx).onPinCodeFetchFailure();
                break;
        }
    }

    @Override
    public void failure(RetrofitError error) {
        if (((CancelableAware) ctx).isSuspended()) return;
        try {
            ((ProgressIndicationAware) ctx).hideProgressView();
        } catch (IllegalArgumentException e) {
            return;
        }
        ((PinCodeAware) ctx).onPinCodeFetchFailure();
    }
}
