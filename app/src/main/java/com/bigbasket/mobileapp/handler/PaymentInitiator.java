package com.bigbasket.mobileapp.handler;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPrepaidPaymentResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.payment.PayuPaymentAware;
import com.bigbasket.mobileapp.interfaces.payment.PowerPayPaymentAware;
import com.bigbasket.mobileapp.model.order.PowerPayPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaymentInitiator<T> {
    private T ctx;
    private String potentialOrderId;
    private String paymentMethod;
    private String amount;

    public PaymentInitiator(T ctx, String potentialOrderId, String paymentMethod, String amount) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public void initiate() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        bigBasketApiService.getPrepaidPaymentParams(potentialOrderId, paymentMethod,
                amount, new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPrepaidPaymentResponse> getPrepaidPaymentApiResponse, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                switch (paymentMethod) {
                                    case Constants.PAYU:
                                        constructPayuParams(getPrepaidPaymentApiResponse.apiResponseContent.postParams);
                                        break;
                                    case Constants.HDFC_POWER_PAY:
                                        constructHdfcPowerPayParams(getPrepaidPaymentApiResponse.apiResponseContent.postParams);
                                        break;
                                }
                                break;
                            default:
                                ((HandlerAware) ctx).getHandler()
                                        .sendEmptyMessage(getPrepaidPaymentApiResponse.status, getPrepaidPaymentApiResponse.message);
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
                    }
                });
    }

    private void constructHdfcPowerPayParams(HashMap<String, String> paymentParams) {
        Gson gson = new Gson();
        String json = gson.toJson(paymentParams);
        Type type = new TypeToken<PowerPayPostParams>() {
        }.getType();
        PowerPayPostParams powerPayPostParams = gson.fromJson(json, type);
        ((PowerPayPaymentAware) ctx).initializeHDFCPowerPay(powerPayPostParams);
    }

    private void constructPayuParams(HashMap<String, String> paymentParams) {
        ((PayuPaymentAware) ctx).initializePayu(paymentParams);
    }
}
