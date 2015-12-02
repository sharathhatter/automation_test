package com.bigbasket.mobileapp.handler.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ValidateOrderPaymentApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.util.ApiErrorCodes;

import retrofit.Call;

public class ValidatePaymentHandler<T extends AppOperationAware> {
    private T ctx;
    private String potentialOrderId;
    private String txnId;
    private String fullOrderId;

    public ValidatePaymentHandler(T ctx, String potentialOrderId, String txnId,
                                  String fullOrderId) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.txnId = txnId;
        this.fullOrderId = fullOrderId;
    }

    public void start() {
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(ctx.getCurrentActivity());
        ctx.showProgressDialog("Validating Payment...");
        Call<ApiResponse<ValidateOrderPaymentApiResponse>> call = bigBasketApiService.validateOrderPayment(txnId, potentialOrderId, fullOrderId);
        call.enqueue(new BBNetworkCallback<ApiResponse<ValidateOrderPaymentApiResponse>>(ctx) {
            @Override
            public void onSuccess(ApiResponse<ValidateOrderPaymentApiResponse> validateOrderPaymentResponse) {
                switch (validateOrderPaymentResponse.status) {
                    case 0:
                        if (ctx instanceof OnPaymentValidationListener) {
                            ((OnPaymentValidationListener) ctx).onPaymentValidated(true, null,
                                    validateOrderPaymentResponse.apiResponseContent.orders);
                        }
                        break;
                    case ApiErrorCodes.PAYMENT_ERROR:
                        if (ctx instanceof OnPaymentValidationListener) {
                            ((OnPaymentValidationListener) ctx).onPaymentValidated(false,
                                    validateOrderPaymentResponse.message,
                                    validateOrderPaymentResponse.apiResponseContent.orders);
                        }
                        break;
                    default:
                        ctx.getHandler().sendEmptyMessage(validateOrderPaymentResponse.status,
                                validateOrderPaymentResponse.message);
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
    }
}
