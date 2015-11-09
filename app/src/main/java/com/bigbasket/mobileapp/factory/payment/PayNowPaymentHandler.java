package com.bigbasket.mobileapp.factory.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPayzappPaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPrepaidPaymentResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.util.Constants;

import retrofit.Call;

public class PayNowPaymentHandler<T extends AppOperationAware> extends PaymentHandler<T> {

    public PayNowPaymentHandler(T ctx, String potentialOrderId, String orderId,
                                String paymentMethod, boolean isPayNow, boolean isFundWallet) {
        super(ctx, potentialOrderId, orderId, paymentMethod, isPayNow, isFundWallet);
    }

    @Override
    public void initiate() {
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(ctx.getCurrentActivity());
        switch (paymentMethod) {
            case Constants.HDFC_POWER_PAY:
                Call<ApiResponse<GetPayzappPaymentParamsResponse>> call = bigBasketApiService.postPayzappPayNowDetails(orderId, paymentMethod);
                call.enqueue(new BBNetworkCallback<ApiResponse<GetPayzappPaymentParamsResponse>>(ctx) {
                    @Override
                    public void onSuccess(ApiResponse<GetPayzappPaymentParamsResponse> getPayzappPaymentParamsApiResponse) {
                        switch (getPayzappPaymentParamsApiResponse.status) {
                            case 0:
                                openPayzappGateway(getPayzappPaymentParamsApiResponse.apiResponseContent.payzappPostParams);
                                break;
                            default:
                                ctx.getHandler().sendEmptyMessage(getPayzappPaymentParamsApiResponse.status,
                                        getPayzappPaymentParamsApiResponse.message);
                                break;
                        }
                    }

                    @Override
                    public boolean updateProgress() {
                        try {
                            ctx.hideProgressDialog();
                            return true;
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    }
                });
                break;
            default:
                Call<ApiResponse<GetPrepaidPaymentResponse>> callOther = bigBasketApiService.postPayNowDetails(orderId, paymentMethod);
                callOther.enqueue(new BBNetworkCallback<ApiResponse<GetPrepaidPaymentResponse>>(ctx) {
                    @Override
                    public void onSuccess(ApiResponse<GetPrepaidPaymentResponse> getPrepaidPaymentApiResponse) {
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                openGateway(getPrepaidPaymentApiResponse.apiResponseContent.postParams);
                                break;
                            default:
                                ctx.getHandler().sendEmptyMessage(getPrepaidPaymentApiResponse.status,
                                        getPrepaidPaymentApiResponse.message);
                                break;
                        }
                    }

                    @Override
                    public boolean updateProgress() {
                        try {
                            ctx.hideProgressDialog();
                            return true;
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    }
                });
                break;
        }
    }

}
