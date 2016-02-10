package com.bigbasket.mobileapp.factory.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PayzappPrePaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PrePaymentParamsResponse;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;

import retrofit2.Call;

public class PayNowPrepaymentProcessingTask<T extends AppOperationAware>
        extends AbstractPrepaymentProcessingTask<T> {
    public PayNowPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                          String paymentMethod, boolean isPayNow,
                                          boolean isFundWallet, boolean showPayUOption) {
        super(ctx, potentialOrderId, orderId, paymentMethod, isPayNow, isFundWallet, showPayUOption);
    }

    @Override
    protected Call<ApiResponse<PayzappPrePaymentParamsResponse>> getPayzappPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService) {
        return bigBasketApiService.postPayzappPayNowDetails(ctx.getCurrentActivity().getPreviousScreenName(), orderId, paymentMethod);
    }

    @Override
    protected Call<ApiResponse<PrePaymentParamsResponse>> getPrepaymentParamsApiCall(BigBasketApiService bigBasketApiService) {
        return bigBasketApiService.postPayNowDetails(ctx.getCurrentActivity().getPreviousScreenName(), orderId, paymentMethod);
    }
}