package com.bigbasket.mobileapp.factory.payment;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ValidateOrderPaymentApiResponse;
import com.bigbasket.mobileapp.factory.payment.impl.MobikwikPayment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.crashlytics.android.Crashlytics;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;
import com.mobikwik.sdk.MobikwikSDK;
import com.mobikwik.sdk.lib.MKTransactionResponse;
import com.payu.india.Payu.PayuConstants;

import java.util.HashMap;

import retrofit.Call;

public final class ValidatePayment<T extends AppOperationAware> {
    private T context;
    private ValidatePaymentRequest validatePaymentRequest;
    @Nullable
    private BigBasketMessageHandler handler;

    public ValidatePayment(T context,
                           ValidatePaymentRequest validatePaymentRequest,
                           @Nullable BigBasketMessageHandler handler) {
        this.context = context;
        this.validatePaymentRequest = validatePaymentRequest;
        this.handler = handler;
    }

    public ValidatePayment(T context, ValidatePaymentRequest validatePaymentRequest) {
        this(context, validatePaymentRequest, null);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case WibmoSDK.REQUEST_CODE_IAP_PAY:
                if (TextUtils.isEmpty(validatePaymentRequest.getTxnId())) {
                    throw new IllegalArgumentException("txn_id can't be empty for Payzapp");
                }
                validatePayzapp(data, resultCode);
                return true;
            case PayuConstants.PAYU_REQUEST_CODE:
                if (TextUtils.isEmpty(validatePaymentRequest.getTxnId())) {
                    throw new IllegalArgumentException("txn_id can't be empty for PayU");
                }
                validate(null);
                return true;
            case MobikwikPayment.MOBIKWIK_REQ_CODE:
                if (data != null) {
                    MKTransactionResponse response =
                            (MKTransactionResponse) data.getSerializableExtra(
                                    MobikwikSDK.EXTRA_TRANSACTION_RESPONSE);
                    if (response != null) {
                        if (!TextUtils.isEmpty(response.orderId)) {
                            try {
                                validatePaymentRequest.setTxnId(response.orderId);
                                validate(null);
                            } catch (NumberFormatException e) {
                                Crashlytics.logException(e);
                            }
                        } else {
                            Crashlytics.logException(new IllegalArgumentException("OrderID is empty for Mobikwik payment"));
                        }
                    } else {
                        Crashlytics.logException(new IllegalArgumentException("No Mobikwik response"));
                    }
                }
                return true;
            case NavigationCodes.RC_PAY_FROM_BB_WALLET:
                validate(null);
                return true;
            default:
                return false;
        }

    }

    public void validatePaytm(boolean status, HashMap<String, String> additionalParams) {
        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }
        additionalParams.put(Constants.STATUS, status ? "1" : "0");
        validate(additionalParams);
    }

    private void validatePayzapp(Intent data, int resultCode) {
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.AMOUNT, validatePaymentRequest.getFinalTotal());
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

        validate(queryMap);
    }

    public void validate(@Nullable HashMap<String, String> additionalParams) {
        if (!context.checkInternetConnection()) {
            if (handler != null) {
                handler.sendOfflineError();
            } else {
                context.getHandler().sendOfflineError();
            }
            return;
        }
        int resId;
        if (!validatePaymentRequest.isPayNow() && !validatePaymentRequest.isWallet() &&
                TextUtils.isEmpty(validatePaymentRequest.getSelectedPaymentMethod())) {
            resId = R.string.converting_to_cod;
        } else {
            resId = R.string.validating_payment;
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context.getCurrentActivity());
        context.showProgressDialog(context.getCurrentActivity().getString(resId));
        Call<ApiResponse<ValidateOrderPaymentApiResponse>> call = bigBasketApiService.
                validatePayment(validatePaymentRequest.getTxnId(), validatePaymentRequest.getPotentialOrderId(),
                        validatePaymentRequest.getOrderId(), validatePaymentRequest.getSelectedPaymentMethod(),
                        validatePaymentRequest.isPayNow() ? "1" : "0",
                        validatePaymentRequest.isWallet() ? "1" : "0", additionalParams);
        call.enqueue(new BBNetworkCallback<ApiResponse<ValidateOrderPaymentApiResponse>>(context) {
                         @Override
                         public void onSuccess
                                 (ApiResponse<ValidateOrderPaymentApiResponse> validateOrderPaymentResponse) {
                             switch (validateOrderPaymentResponse.status) {
                                 case 0:
                                     if (context instanceof OnPaymentValidationListener) {
                                         ((OnPaymentValidationListener) context).onPaymentValidated(true, null,
                                                 validateOrderPaymentResponse.apiResponseContent.orders);
                                     }
                                     break;
                                 case ApiErrorCodes.PAYMENT_ERROR:
                                     if (context instanceof OnPaymentValidationListener) {
                                         ((OnPaymentValidationListener) context).onPaymentValidated(false,
                                                 validateOrderPaymentResponse.message,
                                                 validateOrderPaymentResponse.apiResponseContent.orders);
                                     }
                                     break;
                                 default:
                                     if (handler != null) {
                                         handler.sendEmptyMessage(validateOrderPaymentResponse.status,
                                                 validateOrderPaymentResponse.message);
                                     } else {
                                         context.getHandler().sendEmptyMessage(validateOrderPaymentResponse.status,
                                                 validateOrderPaymentResponse.message);
                                     }
                                     break;
                             }
                         }

                         @Override
                         public void onFailure(int httpErrorCode, String msg) {
                             if (handler != null) {
                                 handler.handleHttpError(httpErrorCode, msg, false);
                             } else {
                                 super.onFailure(httpErrorCode, msg);
                             }
                         }

                         @Override
                         public void onFailure(Throwable t) {
                             if (handler != null) {
                                 if (context.isSuspended()) return;
                                 if (!updateProgress()) return;
                                 handler.handleRetrofitError(t, false);
                             } else {
                                 super.onFailure(t);
                             }
                         }

                         @Override
                         public boolean updateProgress() {
                             try {
                                 context.hideProgressDialog();
                                 return true;
                             } catch (IllegalArgumentException e) {
                                 return false;
                             }
                         }
                     }
        );
    }
}
