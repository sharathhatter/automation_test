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
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PayzappPrePaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PrePaymentParamsResponse;
import com.bigbasket.mobileapp.factory.payment.impl.HDFCPayzappPayment;
import com.bigbasket.mobileapp.factory.payment.impl.MobikwikPayment;
import com.bigbasket.mobileapp.factory.payment.impl.PaytmPayment;
import com.bigbasket.mobileapp.factory.payment.impl.PayuPayment;
import com.bigbasket.mobileapp.handler.payment.MobikwikResponseHandler;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.crashlytics.android.Crashlytics;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.payu.india.Payu.PayuConstants;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit.Call;
import retrofit.Response;

public abstract class AbstractPrepaymentProcessingTask<T extends AppOperationAware>
        extends AsyncTask<Void, Long, Boolean> {
    protected T ctx;
    protected String potentialOrderId;
    protected String paymentMethod;
    protected String orderId;
    protected boolean isPayNow;
    protected boolean isFundWallet;
    private MinDurationCountDownTimer minDurationCountDownTimer;
    private CountDownLatch countDownLatch;
    private long minDuation;
    protected ErrorResponse errorResponse;
    protected PrePaymentParamsResponse prePaymentParamsResponse;
    protected PayzappPrePaymentParamsResponse payzappPrePaymentParamsResponse;
    protected Callback callback;
    private boolean isPaused;


    public AbstractPrepaymentProcessingTask(T ctx, String potentialOrderId, String orderId,
                                            String paymentMethod, boolean isPayNow, boolean isFundWallet) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.isPayNow = isPayNow;
        this.isFundWallet = isFundWallet;
    }

    public void setMinDuration(long minDuration) {
        this.minDuation = minDuration;
    }

    public void clearMinDuration(){
        if(minDuation > 0 && minDurationCountDownTimer != null){
            if(!minDurationCountDownTimer.isFinished()){
                minDurationCountDownTimer.cancel();
                minDurationCountDownTimer = null;
                if(countDownLatch != null){
                    countDownLatch.countDown();
                }
            }
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public synchronized void pause(){
        isPaused = true;
    }
    //Must be called from main thread
    public synchronized void resume(){
        if(Looper.getMainLooper() != Looper.myLooper()){
            throw new IllegalStateException("Must be called from main thread");
        }
        if(isPaused){
            isPaused = false;
            Boolean result = null;
            try {
                result = get(0, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException
                    | TimeoutException | CancellationException e) {
                //Ignore
            }
            if(result != null) {
                onPostExecute(result);
            }
        }
    }

    public synchronized boolean isPaused() {
        return isPaused;
    }

    public synchronized @Nullable String getTransactionId(){
        if(payzappPrePaymentParamsResponse != null
                && payzappPrePaymentParamsResponse.payzappPostParams != null){
            return payzappPrePaymentParamsResponse.payzappPostParams.getTxnId();
        }

        if(prePaymentParamsResponse != null && prePaymentParamsResponse.postParams != null){
            String key = null;
            switch (paymentMethod) {
                case Constants.PAYU:
                case Constants.PAYUMONEY_WALLET:
                    key = PayuConstants.TXNID;
                    break;
                case Constants.MOBIKWIK_PAYMENT:
                    key = MobikwikResponseHandler.KEY_TRANS_ID;
                    break;
                case Constants.PAYTM_WALLET:
                    key = Constants.PAYTM_TRANS_ID_KEY;
                    break;
            }
            if(!TextUtils.isEmpty(key)) {
                return prePaymentParamsResponse.postParams.get(key);
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(minDuation > 0) {
            minDurationCountDownTimer = new MinDurationCountDownTimer(minDuation, 500){
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
        if(callback != null){
            callback.onMicDelayTick(values[0]);
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean result = false;
        Context context;
        if(ctx.getCurrentActivity() != null){
            context = ctx.getCurrentActivity().getApplicationContext();
        } else {
            if(minDurationCountDownTimer != null){
                minDurationCountDownTimer.cancel();
            }
          return result;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context);
        countDownLatch = new CountDownLatch(2);
        if(minDurationCountDownTimer != null) {
            minDurationCountDownTimer.start();
        } else {
            countDownLatch.countDown();
        }
        try {
            if (Constants.HDFC_POWER_PAY.equals(paymentMethod)) {
                Call<ApiResponse<PayzappPrePaymentParamsResponse>> call =
                        getPayzappPrepaymentParamsApiCall(bigBasketApiService);
                Response<ApiResponse<PayzappPrePaymentParamsResponse>> response = call.execute();
                if(response.isSuccess()){
                    if(response.body().status == 0) {
                        synchronized (this) {
                            payzappPrePaymentParamsResponse = response.body().apiResponseContent;
                        }
                        PayzappPostParams payzappPostParams = payzappPrePaymentParamsResponse.payzappPostParams;
                        WibmoSDK.setWibmoIntentActionPackage(payzappPostParams.getPkgName());
                        WibmoSDKConfig.setWibmoDomain(payzappPostParams.getServerUrl());
                        WibmoSDK.init(context);
                        countDownLatch.countDown();
                        result = true;
                    } else {
                        errorResponse = new ErrorResponse(-1 * response.body().status,
                                response.body().message, null);
                    }
                } else {
                    errorResponse = new ErrorResponse(response.code(), response.message(),
                            response.errorBody());
                }
            } else {
                Call<ApiResponse<PrePaymentParamsResponse>> call =
                        getPrepaymentParamsApiCall(bigBasketApiService);
                Response<ApiResponse<PrePaymentParamsResponse>> response = call.execute();
                if(response.isSuccess()){
                    if(response.body().status == 0) {
                        synchronized (this) {
                            prePaymentParamsResponse = response.body().apiResponseContent;
                        }
                        countDownLatch.countDown();
                        result = true;
                    } else {
                        errorResponse = new ErrorResponse(-1 * response.body().status,
                                response.body().message, null);
                    }
                } else {
                    errorResponse = new ErrorResponse(response.code(), response.message(),
                            response.errorBody());
                }
            }
        } catch (IOException ex){
            Crashlytics.logException(ex);
            errorResponse = new ErrorResponse(ex);
        }
        if(!result){
            countDownLatch.countDown(); // countdown for network operation
            if(minDurationCountDownTimer != null){
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
        if(isCancelled() || ctx.isSuspended() || isPaused()){
            return;
        }
        if(success){
            try {
                openGateway();
            } catch (IllegalStateException | IllegalArgumentException ex){
                errorResponse = new ErrorResponse(ex);
                success = false;
            }
        }
        if(callback != null) {
            if (success) {
                callback.onSuccess();
            } else {
                callback.onFailure(errorResponse);
            }
        }
    }
    protected void openGateway(){
        HashMap<String, String> paymentParams = null ;
        Activity activity = ctx.getCurrentActivity();
        if (Constants.HDFC_POWER_PAY.equals(paymentMethod)) {
            if(payzappPrePaymentParamsResponse == null ||
                    payzappPrePaymentParamsResponse.payzappPostParams == null){
                throw new IllegalStateException("Payzapp prepayment params are null");
            }
        } else {
            if(prePaymentParamsResponse == null ||
                    prePaymentParamsResponse.postParams == null){
                throw new IllegalStateException("Prepayment params are null");
            } else {
                paymentParams  = prePaymentParamsResponse.postParams;
            }
        }
        switch (paymentMethod) {
            case Constants.PAYU:
            case Constants.PAYUMONEY_WALLET:
                PayuPayment.startPaymentGateway(paymentParams, activity);
                break;
            case Constants.MOBIKWIK_PAYMENT:
                MobikwikPayment.startPaymentGateway(paymentParams, activity);
                break;
            case Constants.PAYTM_WALLET:
                PaytmPayment.startPaymentGateway(paymentParams, activity,
                        potentialOrderId, orderId, isPayNow, isFundWallet);
                break;
            case Constants.HDFC_POWER_PAY:
                HDFCPayzappPayment.startHDFCPayzapp(
                        payzappPrePaymentParamsResponse.payzappPostParams,
                        activity);
                break;
        }

    }

    protected abstract Call<ApiResponse<PayzappPrePaymentParamsResponse>> getPayzappPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService);

    protected abstract Call<ApiResponse<PrePaymentParamsResponse>> getPrepaymentParamsApiCall(
            BigBasketApiService bigBasketApiService);

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

    public static class ErrorResponse {
        private int code;
        private String message;
        private ResponseBody errorResponseBody;
        private Throwable throwable;

        public ErrorResponse(int code, String message, ResponseBody errorResponseBody) {
            this.code = code;
            this.message = message;
            this.errorResponseBody = errorResponseBody;
        }

        public ErrorResponse(Throwable throwable) {
            this.throwable = throwable;
        }

        public boolean isException(){
            return throwable != null;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public ResponseBody getErrorResponseBody() {
            return errorResponseBody;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    public interface Callback {
        void onSuccess();
        void onFailure(ErrorResponse errorResponse);
        void onMicDelayTick(long millisUntilFinished);
    }
}