package com.bigbasket.mobileapp.handler.payment;

import android.support.annotation.Nullable;
import android.text.TextUtils;

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
import com.bigbasket.mobileapp.util.Constants;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SendPaymentResponseTask<T> {
    private T ctx;
    private String potentialOrderId;
    private String paymentType;
    private String txnId;
    private boolean status;
    private String isPayNow;
    private String isWallet;

    // For HDFC Power Pay
    private String pgTxnId;
    private String dataPickupCode;
    private String errResCode;
    private String errResDesc;
    private String amount;
    private String orderId;

    // For PAYTM
    private HashMap<String, String> paytmParams;

    public SendPaymentResponseTask(T ctx, @Nullable String potentialOrderId, String paymentType,
                                   boolean status, @Nullable String orderId) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentType = paymentType;
        this.status = status;
        this.orderId = orderId;
    }

    public SendPaymentResponseTask setPayNow(boolean payNow) {
        this.isPayNow = payNow ? "1" : "0";
        return this;
    }

    public SendPaymentResponseTask isWallet(boolean isWallet) {
        this.isWallet = isWallet ? "1" : "0";
        return this;
    }

    public SendPaymentResponseTask setPgTxnId(String pgTxnId) {
        this.pgTxnId = pgTxnId;
        return this;
    }

    public SendPaymentResponseTask setDataPickupCode(String dataPickupCode) {
        this.dataPickupCode = dataPickupCode;
        return this;
    }

    public SendPaymentResponseTask setErrResCode(String errResCode) {
        this.errResCode = errResCode;
        return this;
    }

    public SendPaymentResponseTask setErrResDesc(String errResDesc) {
        this.errResDesc = errResDesc;
        return this;
    }

    public SendPaymentResponseTask setTxnId(String txnId) {
        this.txnId = txnId;
        return this;
    }

    public SendPaymentResponseTask setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public SendPaymentResponseTask setPayTmParams(HashMap<String, String> paytmParams) {
        this.paytmParams = paytmParams;
        return this;
    }

    public void start() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.PAYMENT_TYPE, paymentType);
        queryMap.put(Constants.STATUS, status ? "1" : "0");
        queryMap.put(Constants.PAY_NOW, isPayNow);
        queryMap.put(Constants.WALLET, isWallet);
        if (!TextUtils.isEmpty(potentialOrderId)) {
            queryMap.put(Constants.P_ORDER_ID, potentialOrderId);
        }
        if (!TextUtils.isEmpty(Constants.ORDER_ID)) {
            queryMap.put(Constants.ORDER_ID, orderId);
        }

        switch (paymentType) {
            case Constants.HDFC_POWER_PAY:
                queryMap.put(Constants.TXN_ID, txnId);
                if (!TextUtils.isEmpty(amount)) {
                    queryMap.put(Constants.AMOUNT, amount);
                }
                if (status) {
                    queryMap.put(Constants.PG_TXN_ID, pgTxnId);
                    queryMap.put(Constants.DATA_PICKUP_CODE, dataPickupCode);
                } else {
                    queryMap.put(Constants.ERR_RES_CODE, errResCode);
                    queryMap.put(Constants.ERR_RES_DESC, errResDesc);
                }
                break;
            case Constants.PAYTM_WALLET:
                queryMap.putAll(paytmParams);
                break;
        }
        bigBasketApiService.postPrepaidPayment(queryMap, new PostPrepaidParamsCallback());
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
                        ((OnPostPaymentListener) ctx).onPostPaymentSuccess(txnId, paymentType);
                    } else {
                        ((OnPostPaymentListener) ctx).onPostPaymentFailure(txnId, paymentType);
                    }
                    break;
                default:
                    ((HandlerAware) ctx).getHandler().sendEmptyMessage(postPrepaidPaymentApiResponse.status,
                            postPrepaidPaymentApiResponse.message);
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
