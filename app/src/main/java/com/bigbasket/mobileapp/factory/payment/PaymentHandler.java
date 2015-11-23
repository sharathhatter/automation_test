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
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;

import java.util.HashMap;

import retrofit.Call;

public class PaymentHandler<T extends AppOperationAware> {
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
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(ctx.getCurrentActivity());
        switch (paymentMethod) {
            case Constants.HDFC_POWER_PAY:
                Call<ApiResponse<GetPayzappPaymentParamsResponse>> call = bigBasketApiService.getPayzappOrderPaymentParams(potentialOrderId);
                call.enqueue(new BBNetworkCallback<ApiResponse<GetPayzappPaymentParamsResponse>>(ctx) {
                    @Override
                    public void onSuccess(ApiResponse<GetPayzappPaymentParamsResponse> getPrepaidPaymentApiResponse) {
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                openPayzappGateway(getPrepaidPaymentApiResponse.apiResponseContent.payzappPostParams);
                                break;
                            default:
                                ctx.getHandler()
                                        .sendEmptyMessage(getPrepaidPaymentApiResponse.status, getPrepaidPaymentApiResponse.message);
                                break;
                        }
                    }

                    @Override
                    public boolean updateProgress() {
                        return true;
                    }
                });
                break;
            default:
                Call<ApiResponse<GetPrepaidPaymentResponse>> callOther = bigBasketApiService.getOrderPaymentParams(potentialOrderId);
                callOther.enqueue(new BBNetworkCallback<ApiResponse<GetPrepaidPaymentResponse>>(ctx) {
                    @Override
                    public void onSuccess(ApiResponse<GetPrepaidPaymentResponse> getPrepaidPaymentApiResponse) {
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                openGateway(getPrepaidPaymentApiResponse.apiResponseContent.postParams);
                                break;
                            default:
                                ctx.getHandler()
                                        .sendEmptyMessage(getPrepaidPaymentApiResponse.status, getPrepaidPaymentApiResponse.message);
                                break;
                        }
                    }

                    @Override
                    public boolean updateProgress() {
                        return true;
                    }
                });
                break;
        }
    }

    protected void openGateway(HashMap<String, String> paymentParams) {

        switch (paymentMethod) {
            case Constants.PAYU:
            case Constants.PAYUMONEY_WALLET:
                PayuPayment.startPaymentGateway(paymentParams, ctx.getCurrentActivity());
                break;
            case Constants.MOBIKWIK_PAYMENT:
                new MobikwikPayment().startPaymentGateway(paymentParams, ctx.getCurrentActivity());
                break;
            case Constants.PAYTM_WALLET:
                new PaytmPayment().startPaymentGateway(paymentParams, ctx.getCurrentActivity(),
                        potentialOrderId, orderId, isPayNow, isFundWallet);
                break;
        }
    }

    protected void openPayzappGateway(PayzappPostParams payzappPostParams) {
        new HDFCPayzappPayment().startPaymentGateway(payzappPostParams, ctx.getCurrentActivity());
    }
}
