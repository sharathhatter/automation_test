package com.bigbasket.mobileapp.handler.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.util.ApiErrorCodes;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ValidatePaymentHandler<T> {
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
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Validating Payment...");
        bigBasketApiService.validateOrderPayment(txnId, potentialOrderId, fullOrderId, new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse validateOrderPaymentResponse, Response response) {
                if (((CancelableAware) ctx).isSuspended()) return;
                try {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (validateOrderPaymentResponse.status) {
                    case 0:
                        ((OnPaymentValidationListener) ctx).onPaymentValidated(true, null);
                        break;
                    case ApiErrorCodes.PAYMENT_ERROR:
                        ((OnPaymentValidationListener) ctx).onPaymentValidated(false,
                                validateOrderPaymentResponse.message);
                    default:
                        ((HandlerAware) ctx).getHandler().sendEmptyMessage(validateOrderPaymentResponse.status,
                                validateOrderPaymentResponse.message);
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
}
