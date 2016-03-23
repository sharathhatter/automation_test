package com.bigbasket.mobileapp.factory.payment;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PayzappPrePaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PrePaymentParamsResponse;
import com.bigbasket.mobileapp.factory.payment.impl.HDFCPayzappPayment;
import com.bigbasket.mobileapp.factory.payment.impl.MobikwikPayment;
import com.bigbasket.mobileapp.factory.payment.impl.PaytmPayment;
import com.bigbasket.mobileapp.factory.payment.impl.PayuPayment;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.crashlytics.android.Crashlytics;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.payu.india.Payu.PayuConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Response;

public abstract class AbstractPrepaymentProcessingTask<T extends AppOperationAware>
        extends AsyncTask<Void, Long, Boolean> {
    public String txnOrderId;
    protected T ctx;
    protected String potentialOrderId;
    protected String paymentMethod;
    protected String orderId;
    protected boolean isPayNow;
    protected boolean isFundWallet;
    protected ErrorResponse errorResponse;
    protected Callback callback;
    protected boolean isPaymentParamsAlreadyAvailable;
    protected HashMap<String, String> mPaymentPostParams;
    protected PayzappPostParams mPayzappPostParams;
    private MinDurationCountDownTimer minDurationCountDownTimer;
    private CountDownLatch countDownLatch;
    private long minDuation;
    private boolean isPaused;
    private boolean isPayUOptionVisible;
    protected int wallet;
    protected boolean paymentGatewayOpened = false;

    public AbstractPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                            String paymentMethod, boolean isPayNow, boolean isFundWallet, boolean isPayUOptionVisible, int wallet) {

        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.isPayNow = isPayNow;
        this.isFundWallet = isFundWallet;
        this.isPayUOptionVisible = isPayUOptionVisible;
        this.isPaymentParamsAlreadyAvailable = false;
        this.wallet = wallet;
    }

    public AbstractPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                            String paymentMethod, boolean isPayNow, boolean isFundWallet, boolean isPayUOptionVisible,
                                            HashMap<String, String> mPaymentPostParams,
                                            PayzappPostParams mPayzappPostParams) {

        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.isPayNow = isPayNow;
        this.isFundWallet = isFundWallet;
        this.isPayUOptionVisible = isPayUOptionVisible;
        this.mPaymentPostParams = mPaymentPostParams;
        this.mPayzappPostParams = mPayzappPostParams;
        this.isPaymentParamsAlreadyAvailable = true;
    }

    public AbstractPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                            String paymentMethod, boolean isPayNow, boolean isFundWallet, boolean isPayUOptionVisible) {

        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.isPayNow = isPayNow;
        this.isFundWallet = isFundWallet;
        this.isPayUOptionVisible = isPayUOptionVisible;
        this.isPaymentParamsAlreadyAvailable = false;
    }


    public void setMinDuration(long minDuration) {
        this.minDuation = minDuration;
    }

    public void clearMinDuration() {
        if (minDuation > 0 && minDurationCountDownTimer != null) {
            if (!minDurationCountDownTimer.isFinished()) {
                minDurationCountDownTimer.cancel();
                minDurationCountDownTimer = null;
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }
            }
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public synchronized void pause() {
        isPaused = true;
    }

    //Must be called from main thread
    public synchronized void resume() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("Must be called from main thread");
        }
        if (isPaused) {
            isPaused = false;
            Boolean result = null;
            try {
                result = get(0, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException
                    | TimeoutException | CancellationException e) {
                //Ignore
            }
            if (result != null) {
                onPostExecute(result);
            }
        }
    }

    public synchronized boolean isPaused() {
        return isPaused;
    }

    public String getTxnOrderId() {
        return txnOrderId;
    }

    public void setTxnOrderId(String txnOrderId) {
        this.txnOrderId = txnOrderId;
    }

    public synchronized
    @Nullable
    String getTransactionId() {
        if (mPayzappPostParams != null) {
            return mPayzappPostParams.getTxnId();
        }

        if (mPaymentPostParams != null) {
            String key = null;
            switch (paymentMethod) {
                case Constants.PAYU:
                case Constants.PAYUMONEY_WALLET:
                    key = PayuConstants.TXNID;
                    break;
                case Constants.MOBIKWIK_WALLET:
                    key = MobikwikPayment.MOBIKWIK_ORDERID;
                    break;
                case Constants.PAYTM_WALLET:
                    key = PaytmPayment.TXN_ID;
                    break;
            }
            if (!TextUtils.isEmpty(key)) {
                return mPaymentPostParams.get(key);
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (minDuation > 0) {
            minDurationCountDownTimer = new MinDurationCountDownTimer(minDuation, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    super.onTick(millisUntilFinished);
                    publishProgress(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    countDownLatch.countDown();
                }
            };
        }
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        if (callback != null) {
            callback.onMicDelayTick(values[0]);
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean result = false;
        Context context;
        if (ctx.getCurrentActivity() != null) {
            context = ctx.getCurrentActivity().getApplicationContext();
        } else {
            if (minDurationCountDownTimer != null) {
                minDurationCountDownTimer.cancel();
            }
            return result;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
        countDownLatch = new CountDownLatch(2);
        if (minDurationCountDownTimer != null) {
            minDurationCountDownTimer.start();
        } else {
            countDownLatch.countDown();
        }
        /**
         * condition:
         * 1. if isPaymentParamsAlreadyAvailable=false: means the task doesnt have the payment parameters
         * call the service to get the payment parameters based on the payment method
         *
         * 2. if isPaymentParamsAlreadyAvailable=true, but the payment parameters are null(may be the task has been called with sending null as the parameters)
         * call the service to get the payment parameters based on the payment method
         *
         */
        if (!isPaymentParamsAlreadyAvailable || (mPayzappPostParams == null && mPaymentPostParams == null)) {
            try {
                if (Constants.HDFC_POWER_PAY.equals(paymentMethod)) {
                    Call<ApiResponse<PayzappPrePaymentParamsResponse>> call =
                            getPayzappPrepaymentParamsApiCall(bigBasketApiService);
                    Response<ApiResponse<PayzappPrePaymentParamsResponse>> response = call.execute();
                    if (response.isSuccessful()) {
                        if (response.body().status == 0) {
                            synchronized (this) {
                                mPayzappPostParams = response.body().apiResponseContent.payzappPostParams;
                                txnOrderId = response.body().apiResponseContent.txnOrderId;
                                setTxnOrderId(txnOrderId);
                            }
                            WibmoSDK.setWibmoIntentActionPackage(mPayzappPostParams.getPkgName());
                            WibmoSDKConfig.setWibmoDomain(mPayzappPostParams.getServerUrl());
                            WibmoSDK.init(context);
                            countDownLatch.countDown();
                            result = true;
                        } else {
                            errorResponse = new ErrorResponse(response.body().status,
                                    response.body().message, ErrorResponse.API_ERROR);
                        }
                    } else {
                        errorResponse = new ErrorResponse(response.code(), response.message(),
                                ErrorResponse.HTTP_ERROR);
                    }
                } else {
                    Call<ApiResponse<PrePaymentParamsResponse>> call =
                            getPrepaymentParamsApiCall(bigBasketApiService);
                    Response<ApiResponse<PrePaymentParamsResponse>> response = call.execute();
                    if (response.isSuccessful()) {
                        if (response.body().status == 0) {
                            synchronized (this) {
                                mPaymentPostParams = response.body().apiResponseContent.postParams;
                                txnOrderId = response.body().apiResponseContent.txnOrderId;
                                setTxnOrderId(txnOrderId);
                            }
                            countDownLatch.countDown();
                            result = true;
                        } else {
                            errorResponse = new ErrorResponse(response.body().status,
                                    response.body().message, ErrorResponse.API_ERROR);
                        }
                    } else {
                        errorResponse = new ErrorResponse(response.code(), response.message(),
                                ErrorResponse.HTTP_ERROR);
                    }
                }
            } catch (IOException ex) {
                Crashlytics.logException(ex);
                errorResponse = new ErrorResponse(ex);
            }
        } else {
            result = true;
            countDownLatch.countDown();
            //initializing the PayZapp SDK
            if (Constants.HDFC_POWER_PAY.equals(paymentMethod)) {
                WibmoSDK.setWibmoIntentActionPackage(mPayzappPostParams.getPkgName());
                WibmoSDKConfig.setWibmoDomain(mPayzappPostParams.getServerUrl());
                WibmoSDK.init(context);
            }
        }
        if (!result) {
            countDownLatch.countDown(); // countdown for network operation
            if (minDurationCountDownTimer != null) {
                minDurationCountDownTimer.cancel();
                countDownLatch.countDown(); //For delay timer
            }
        } else {
            try {
                countDownLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                result = false;
                errorResponse = new ErrorResponse(ex);
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (isCancelled() || ctx.isSuspended() || isPaused()) {
            return;
        }
        if (success) {
            /**payment parameters is already passed in the constructor
             * open the gateway method based on that.
             */

            try {
                openGateway();
            } catch (IllegalStateException | IllegalArgumentException ex) {
                errorResponse = new ErrorResponse(ex);
                success = false;
            }
        }

        if (callback != null) {
            if (success) {
                callback.onSuccess();
            } else {
                callback.onFailure(errorResponse);
            }
        }
    }

    /***
     * startPaymentGateway passing the payment method
     */
    protected void openGateway() {
        if (Constants.HDFC_POWER_PAY.equals(paymentMethod)) {
            if (mPayzappPostParams == null) {
                throw new IllegalStateException("Payzapp prepayment params are null");
            }
        } else {
            if (mPaymentPostParams == null) {
                throw new IllegalStateException("Prepayment params are null");
            }
        }
        startPaymentGateway(paymentMethod);
    }


    /**
     * check payment method and initialize the corresponding payment sdk
     *
     * @param paymentMethod:String
     */
    private void startPaymentGateway(String paymentMethod) {
        paymentGatewayOpened = true;
        Activity activity = ctx.getCurrentActivity();
        switch (paymentMethod) {
            case Constants.PAYU:
                PayuPayment.startPaymentGateway(mPaymentPostParams, activity, isPayUOptionVisible, false);
                break;
            case Constants.PAYUMONEY_WALLET:
                PayuPayment.startPaymentGateway(mPaymentPostParams, activity, isPayUOptionVisible, true);
                break;
            case Constants.MOBIKWIK_WALLET:
                MobikwikPayment.startPaymentGateway(mPaymentPostParams, activity);
                break;
            case Constants.PAYTM_WALLET:
                PaytmPayment.startPaymentGateway(mPaymentPostParams, activity,
                        potentialOrderId, getTxnOrderId(), isPayNow, isFundWallet);
                break;
            case Constants.HDFC_POWER_PAY:
                HDFCPayzappPayment.startHDFCPayzapp(
                        mPayzappPostParams,
                        activity);
                break;
            case Constants.BB_WALLET:
                //TODO: invoke onActivityResult
            default:
                paymentGatewayOpened = false;
        }
    }

    protected abstract Call<ApiResponse<PayzappPrePaymentParamsResponse>> getPayzappPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService);

    protected abstract Call<ApiResponse<PrePaymentParamsResponse>> getPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService);

    public interface Callback {
        void onSuccess();

        void onFailure(ErrorResponse errorResponse);

        void onMicDelayTick(long millisUntilFinished);
    }

    private static class MinDurationCountDownTimer extends CountDownTimer {

        private boolean isFinished;

        public MinDurationCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            isFinished = true;
        }

        public boolean isFinished() {
            return isFinished;
        }
    }
}