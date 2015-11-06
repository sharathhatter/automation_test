package com.bigbasket.mobileapp.factory.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPayzappPaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPrepaidPaymentResponse;
import com.bigbasket.mobileapp.factory.payment.impl.HDFCPayzappPayment;
import com.bigbasket.mobileapp.factory.payment.impl.MobikwikPayment;
import com.bigbasket.mobileapp.factory.payment.impl.PaytmPayment;
import com.bigbasket.mobileapp.factory.payment.impl.PayuPayment;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaymentHandler<T> {
    protected T ctx;
    protected String potentialOrderId;
    protected String paymentMethod;
    protected String orderId;
    private boolean isPayNow;
    private boolean isFundWallet;

    public PaymentHandler(T ctx, String potentialOrderId, String orderId,
                          String paymentMethod, boolean isPayNow, boolean isFundWallet) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.isFundWallet = isFundWallet;
        this.isPayNow = isPayNow;
    }

    public void initiate() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        switch (paymentMethod) {
            case Constants.HDFC_POWER_PAY:
                bigBasketApiService.getPayzappOrderPaymentParams(potentialOrderId, new Callback<ApiResponse<GetPayzappPaymentParamsResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPayzappPaymentParamsResponse> getPrepaidPaymentApiResponse, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                openPayzappGateway(getPrepaidPaymentApiResponse.apiResponseContent.payzappPostParams);
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
                        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
                    }
                });
                break;
            default:
                bigBasketApiService.getOrderPaymentParams(potentialOrderId, new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPrepaidPaymentResponse> getPrepaidPaymentApiResponse, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                openGateway(getPrepaidPaymentApiResponse.apiResponseContent.postParams);
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
                        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
                    }
                });
                break;
        }
    }

    protected void openGateway(HashMap<String, String> paymentParams) {

        switch (paymentMethod) {
            case Constants.PAYU:
            case Constants.PAYUMONEY_WALLET:
                new PayuPayment().startPaymentGateway(paymentParams, ((ActivityAware) ctx).getCurrentActivity());
                break;
            case Constants.MOBIKWIK_PAYMENT:
                new MobikwikPayment().startPaymentGateway(paymentParams, ((ActivityAware) ctx).getCurrentActivity());
                break;
            case Constants.PAYTM_WALLET:
                new PaytmPayment().startPaymentGateway(paymentParams, ((ActivityAware) ctx).getCurrentActivity(),
                        potentialOrderId, orderId, isPayNow, isFundWallet);
                break;
        }
    }

    protected void openPayzappGateway(PayzappPostParams payzappPostParams) {
        new HDFCPayzappPayment().startPaymentGateway(payzappPostParams, ((ActivityAware) ctx).getCurrentActivity());
    }
}
