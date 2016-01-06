package com.bigbasket.mobileapp.factory.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PayzappPrePaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PrePaymentParamsResponse;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;

import java.util.HashMap;

import retrofit.Call;

/**
 * Created by bigbasket on 26/11/15.
 */
public class OrderPrepaymentProcessingTask<T extends AppOperationAware>
        extends AbstractPrepaymentProcessingTask<T> {
    public OrderPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                         String paymentMethod, boolean isPayNow,
                                         boolean isFundWallet, boolean isPayUOptionVisible) {
        super(ctx, potentialOrderId, orderId, paymentMethod, isPayNow, isFundWallet, isPayUOptionVisible);
    }

    public OrderPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                         String paymentMethod, boolean isPayNow,
                                         boolean isFundWallet, boolean isPayUOptionVisible,
                                         HashMap<String, String> mPaymentParams,
                                         PayzappPostParams mPayzappPostParams) {
        super(ctx, potentialOrderId, orderId, paymentMethod, isPayNow, isFundWallet, isPayUOptionVisible, mPaymentParams, mPayzappPostParams);
    }


    public void setPaymentParams(HashMap<String, String> mPaymentParams) {
        this.isPaymentParamsAlreadyAvailable = true;
        this.mPaymentPostParams = mPaymentParams;

    }

    public void setPayZappPaymentParams(PayzappPostParams mPayzappPostParams) {
        this.isPaymentParamsAlreadyAvailable = true;
        this.mPayzappPostParams = mPayzappPostParams;

    }

    @Override
    protected Call<ApiResponse<PayzappPrePaymentParamsResponse>> getPayzappPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService) {
        return bigBasketApiService.getPayzappOrderPaymentParams(ctx.getCurrentActivity().getPreviousScreenName(),potentialOrderId);
    }

    @Override
    protected Call<ApiResponse<PrePaymentParamsResponse>> getPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService) {
        return bigBasketApiService.getOrderPaymentParams(ctx.getCurrentActivity().getPreviousScreenName(),potentialOrderId);
    }
}
