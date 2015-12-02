package com.bigbasket.mobileapp.factory.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PayzappPrePaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PrePaymentParamsResponse;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;

import retrofit.Call;

/**
 * Created by bigbasket on 26/11/15.
 */
public class PayNowPrepaymentProcessingTask<T extends AppOperationAware>
        extends AbstractPrepaymentProcessingTask<T> {
    public PayNowPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                          String paymentMethod, boolean isPayNow,
                                          boolean isFundWallet) {
        super(ctx, potentialOrderId, orderId, paymentMethod, isPayNow, isFundWallet);
    }

    @Override
    protected Call<ApiResponse<PayzappPrePaymentParamsResponse>> getPayzappPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService) {
        return bigBasketApiService.postPayzappPayNowDetails(orderId, paymentMethod);
    }

    @Override
    protected Call<ApiResponse<PrePaymentParamsResponse>> getPrepaymentParamsApiCall(BigBasketApiService bigBasketApiService) {
        return bigBasketApiService.postPayNowDetails(orderId, paymentMethod);
    }
}
