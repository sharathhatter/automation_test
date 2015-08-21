package com.bigbasket.mobileapp.apiservice.callbacks;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetAreaInfoResponse;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.task.uiv3.InsertPinCodeAsyncTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CallbackGetAreaInfo<T> implements Callback<ApiResponse<GetAreaInfoResponse>> {

    private T ctx;

    public CallbackGetAreaInfo(T ctx) {
        this.ctx = ctx;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void success(ApiResponse<GetAreaInfoResponse> getAreaInfoResponseApiResponse, Response response) {
        if (((CancelableAware) ctx).isSuspended()) return;
        try {
            ((ProgressIndicationAware) ctx).hideProgressDialog();
        } catch (IllegalArgumentException e) {
            return;
        }
        switch (getAreaInfoResponseApiResponse.status) {
            case 0:
                if (getAreaInfoResponseApiResponse.apiResponseContent.pinCodeMaps != null) {
                    InsertPinCodeAsyncTask task = new InsertPinCodeAsyncTask<>(ctx,
                            getAreaInfoResponseApiResponse.apiResponseContent.pinCodeMaps,
                            getAreaInfoResponseApiResponse.apiResponseContent.cityMap);
                    task.execute();
                    return;
                }
                pinCodeFailure(getAreaInfoResponseApiResponse.status,
                        getAreaInfoResponseApiResponse.message);
                break;
            default:
                pinCodeFailure(getAreaInfoResponseApiResponse.status,
                        getAreaInfoResponseApiResponse.message);
                break;
        }
    }

    @Override
    public void failure(RetrofitError error) {
        pinCodeFailure(error);
    }

    private void pinCodeFailure(RetrofitError error) {
        try {
            ((ProgressIndicationAware) ctx).hideProgressDialog();
        } catch (IllegalArgumentException e) {
            return;
        }
        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
    }

    private void pinCodeFailure(int status, String msg) {
        try {
            ((ProgressIndicationAware) ctx).hideProgressDialog();
        } catch (IllegalArgumentException e) {
            return;
        }
        ((HandlerAware) ctx).getHandler().sendEmptyMessage(status, msg, true);
    }
}
