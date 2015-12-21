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
public class FundWalletPrepaymentProcessingTask<T extends AppOperationAware>
        extends AbstractPrepaymentProcessingTask<T> {
    private String amount;

    public FundWalletPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                              String paymentMethod, boolean isPayNow,
                                              boolean isFundWallet, String amount,boolean showPayUOption) {
        super(ctx, potentialOrderId, orderId, paymentMethod, isPayNow, isFundWallet,showPayUOption);
        this.amount = amount;
    }

    @Override
    protected Call<ApiResponse<PayzappPrePaymentParamsResponse>> getPayzappPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService) {
        return bigBasketApiService.postPayzappFundWallet(paymentMethod, amount);
    }

    @Override
    protected Call<ApiResponse<PrePaymentParamsResponse>> getPrepaymentParamsApiCall(BigBasketApiService bigBasketApiService) {
        return bigBasketApiService.postFundWallet(paymentMethod, amount);
    }
}