package com.bigbasket.mobileapp.handler.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPowerPayPaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPrepaidPaymentResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.payment.PayuPaymentAware;
import com.bigbasket.mobileapp.interfaces.payment.PowerPayPaymentAware;
import com.bigbasket.mobileapp.util.Constants;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaymentInitiator<T> {
    private T ctx;
    private String potentialOrderId;
    private String paymentMethod;

    public PaymentInitiator(T ctx, String potentialOrderId, String paymentMethod) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentMethod = paymentMethod;
    }

    public void initiate() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Redirecting to payment gateway...");
        switch (paymentMethod) {
            case Constants.PAYU:
                bigBasketApiService.getOrderPaymentParams(potentialOrderId, new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
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
                                ((PayuPaymentAware) ctx).initializePayu(getPrepaidPaymentApiResponse.apiResponseContent.postParams);
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
                break;
            case Constants.HDFC_POWER_PAY:
                bigBasketApiService.getPowerPayOrderPaymentParams(potentialOrderId, new Callback<ApiResponse<GetPowerPayPaymentParamsResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPowerPayPaymentParamsResponse> getPrepaidPaymentApiResponse, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                ((PowerPayPaymentAware) ctx).initializeHDFCPowerPay(getPrepaidPaymentApiResponse.apiResponseContent.powerPayPostParams);
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
                break;
            case Constants.MOBIKWIK_PAYMENT:
                bigBasketApiService.getOrderPaymentParams(potentialOrderId, new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
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
                                ((PayuPaymentAware) ctx).initializePayu(getPrepaidPaymentApiResponse.apiResponseContent.postParams);
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
                break;
        }
    }
}
