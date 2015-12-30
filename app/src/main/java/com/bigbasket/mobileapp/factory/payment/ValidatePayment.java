package com.bigbasket.mobileapp.factory.payment;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ValidateOrderPaymentApiResponse;
import com.bigbasket.mobileapp.factory.payment.impl.MobikwikPayment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.crashlytics.android.Crashlytics;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;
import com.mobikwik.sdk.MobikwikSDK;
import com.mobikwik.sdk.lib.MKTransactionResponse;
import com.payu.india.Payu.PayuConstants;

import java.util.HashMap;

import retrofit.Call;

public final class ValidatePayment {
    private ValidatePayment() {
    }

    public static <T extends AppOperationAware & OnPaymentValidationListener> boolean
    onActivityResult(T context, int requestCode, int resultCode, Intent data,
                     @Nullable String txnId, @Nullable String orderId, @Nullable String potentialOrderId,
                     boolean isPayNow, boolean isWallet, @Nullable String finalTotal) {
        switch (requestCode) {
            case WibmoSDK.REQUEST_CODE_IAP_PAY:
                if (TextUtils.isEmpty(txnId)) {
                    throw new IllegalArgumentException("txn_id can't be empty for Payzapp");
                }
                validatePayzapp(context, txnId, orderId, potentialOrderId, isPayNow, isWallet,
                        data, resultCode, finalTotal);
                return true;
            case PayuConstants.PAYU_REQUEST_CODE:
                if (TextUtils.isEmpty(txnId)) {
                    throw new IllegalArgumentException("txn_id can't be empty for PayU");
                }
                validate(context, txnId, orderId, potentialOrderId, isPayNow, isWallet, Constants.PAYU, null);
                return true;
            case MobikwikPayment.MOBIKWIK_REQ_CODE:
                if (data != null) {
                    MKTransactionResponse response = (MKTransactionResponse) data.getSerializableExtra(MobikwikSDK.EXTRA_TRANSACTION_RESPONSE);
                    if (response != null) {
                        if (!TextUtils.isEmpty(response.orderId)) {
                            try {
                                txnId = response.orderId;
                                validate(context, txnId, orderId,
                                        potentialOrderId, isPayNow, isWallet, Constants.MOBIKWIK_WALLET, null);
                            } catch (NumberFormatException e) {
                                Crashlytics.logException(e);
                            }
                        } else {
                            Crashlytics.logException(new IllegalArgumentException());
                        }
                    } else {
                        Crashlytics.logException(new IllegalArgumentException());
                    }
                }
                return true;
            default:
                return false;
        }

    }

    public static <T extends AppOperationAware>
    void validatePaytm(T context, boolean status,
                       @Nullable String txnId, @Nullable String orderId, @Nullable String potentialOrderId,
                       boolean isPayNow, boolean isWallet, HashMap<String, String> additionalParams) {
        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }
        additionalParams.put(Constants.STATUS, status ? "1" : "0");
        validate(context, txnId, orderId, potentialOrderId, isPayNow, isWallet,
                Constants.PAYTM_WALLET, additionalParams);
    }

    private static <T extends AppOperationAware & OnPaymentValidationListener>
    void validatePayzapp(T ctx,
                         String txnId, @Nullable String orderId, @Nullable String potentialOrderId,
                         boolean isPayNow, boolean isWallet, Intent data, int resultCode, String finalTotal) {
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.AMOUNT, finalTotal);
        if (resultCode == Activity.RESULT_OK) {
            WPayResponse res = WibmoSDK.processInAppResponseWPay(data);
            String pgTxnId = res.getWibmoTxnId();
            String dataPickupCode = res.getDataPickUpCode();
            queryMap.put(Constants.PG_TXN_ID, pgTxnId);
            queryMap.put(Constants.DATA_PICKUP_CODE, dataPickupCode);
            queryMap.put(Constants.STATUS, "1");
        } else {
            if (data != null) {
                String resCode = data.getStringExtra("ResCode");
                String resDesc = data.getStringExtra("ResDesc");
                queryMap.put(Constants.ERR_RES_CODE, resCode);
                queryMap.put(Constants.ERR_RES_DESC, resDesc);
            }
            queryMap.put(Constants.STATUS, "0");
        }

        validate(ctx, txnId, orderId, potentialOrderId, isPayNow,
                isWallet, Constants.HDFC_POWER_PAY, queryMap);
    }

    public static <T extends AppOperationAware> void validate(final T ctx,
                                                              String txnId,
                                                              @Nullable String fullOrderId,
                                                              @Nullable String potentialOrderId,
                                                              boolean isPayNow,
                                                              boolean isFundWallet,
                                                              String paymentType,
                                                              @Nullable HashMap<String, String> additionalParams) {
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(ctx.getCurrentActivity());
        ctx.showProgressDialog(ctx.getCurrentActivity().getString(R.string.validating_payment));
        Call<ApiResponse<ValidateOrderPaymentApiResponse>> call = bigBasketApiService.
                validatePayment(txnId, potentialOrderId, fullOrderId, paymentType,
                        isPayNow ? "1" : "0", isFundWallet ? "1" : "0", additionalParams);
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
