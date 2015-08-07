package com.bigbasket.mobileapp.handler.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostPrepaidPaymentResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPostPaymentListener;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PostPaymentHandler<T> {
    private T ctx;
    private String potentialOrderId;
    private String paymentType;
    private String txnId;
    private boolean status;

    // For HDFC Power Pay
    private String pgTxnId;
    private String dataPickupCode;
    private String errResCode;
    private String errResDesc;
    private String amount;

    public PostPaymentHandler(T ctx, String potentialOrderId, String paymentType, String txnId,
                              boolean status, String amount) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentType = paymentType;
        this.txnId = txnId;
        this.status = status;
        this.amount = amount;
    }

    public PostPaymentHandler setPgTxnId(String pgTxnId) {
        this.pgTxnId = pgTxnId;
        return this;
    }

    public PostPaymentHandler setDataPickupCode(String dataPickupCode) {
        this.dataPickupCode = dataPickupCode;
        return this;
    }

    public PostPaymentHandler setErrResCode(String errResCode) {
        this.errResCode = errResCode;
        return this;
    }

    public PostPaymentHandler setErrResDesc(String errResDesc) {
        this.errResDesc = errResDesc;
        return this;
    }

    public void start() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        if (status) {
            bigBasketApiService.postPrepaidPayment(txnId, potentialOrderId, paymentType, "1",
                    pgTxnId, dataPickupCode, amount,
                    new PostPrepaidParamsCallback());
        } else {
            bigBasketApiService.postPrepaidPayment(txnId,
                    potentialOrderId, paymentType, "0",
                    errResCode, errResDesc,
                    new PostPrepaidParamsCallback());
        }
    }

    private class PostPrepaidParamsCallback implements Callback<ApiResponse<PostPrepaidPaymentResponse>> {
        @Override
        public void success(ApiResponse<PostPrepaidPaymentResponse> postPrepaidPaymentApiResponse, Response response) {
            if (((CancelableAware) ctx).isSuspended()) return;
            try {
                ((ProgressIndicationAware) ctx).hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            switch (postPrepaidPaymentApiResponse.status) {
                case 0:
                    if (postPrepaidPaymentApiResponse.apiResponseContent.paymentStatus) {
                        ((OnPostPaymentListener) ctx).onPostPaymentSuccess(txnId);
                    } else {
                        ((OnPostPaymentListener) ctx).onPostPaymentSuccess(txnId);
                    }
                    break;
                default:
                    ((HandlerAware) ctx).getHandler().sendEmptyMessage(postPrepaidPaymentApiResponse.status, postPrepaidPaymentApiResponse.message);
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
    }
}
