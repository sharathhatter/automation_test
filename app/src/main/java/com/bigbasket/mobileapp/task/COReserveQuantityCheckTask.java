package com.bigbasket.mobileapp.task;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.util.Constants;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class COReserveQuantityCheckTask<T> {

    private String pharmaPrescriptionId;
    private T ctx;


    public COReserveQuantityCheckTask(T ctx, String pharmaPrescriptionId) {
        this.ctx = ctx;
        this.pharmaPrescriptionId = pharmaPrescriptionId;
    }

    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        bigBasketApiService.coReserveQuantity(pharmaPrescriptionId,
                new Callback<OldApiResponse<COReserveQuantity>>() {
                    @Override
                    public void success(OldApiResponse<COReserveQuantity> coReserveQuantityOldApiResponse, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) {
                            return;
                        } else {
                            try {
                                ((ProgressIndicationAware) ctx).hideProgressDialog();
                            } catch (IllegalArgumentException ex) {
                                return;
                            }
                        }
                        switch (coReserveQuantityOldApiResponse.status) {
                            case Constants.OK:
                                coReserveQuantityOldApiResponse.apiResponseContent.setStatus(true);
                                int qcLen = coReserveQuantityOldApiResponse.apiResponseContent.getQcErrorData() != null ?
                                        coReserveQuantityOldApiResponse.apiResponseContent.getQcErrorData().size() : 0;
                                coReserveQuantityOldApiResponse.apiResponseContent.setQc_len(qcLen);
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(((ActivityAware) ctx).getCurrentActivity()).edit();
                                editor.putString(Constants.POTENTIAL_ORDER_ID, String.valueOf(coReserveQuantityOldApiResponse.apiResponseContent.getPotentialOrderId()));
                                editor.commit();
                                ((COReserveQuantityCheckAware) ctx).setCOReserveQuantity(coReserveQuantityOldApiResponse.apiResponseContent);
                                ((COReserveQuantityCheckAware) ctx).onCOReserveQuantityCheck();
                                break;
                            case Constants.ERROR:
                                ((HandlerAware) ctx).getHandler().sendEmptyMessage(coReserveQuantityOldApiResponse.getErrorTypeAsInt(),
                                        coReserveQuantityOldApiResponse.message);
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (((CancelableAware) ctx).isSuspended()) {
                            return;
                        } else {
                            try {
                                ((ProgressIndicationAware) ctx).hideProgressDialog();
                            } catch (IllegalArgumentException ex) {
                                return;
                            }
                        }
                        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
                    }
                });
    }
}
