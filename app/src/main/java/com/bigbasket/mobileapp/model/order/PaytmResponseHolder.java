package com.bigbasket.mobileapp.model.order;

import android.os.Bundle;

import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.factory.payment.ValidatePayment;
import com.bigbasket.mobileapp.handler.BigBasketRetryMessageHandler;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class PaytmResponseHolder {
    private static volatile PaytmResponseHolder paytmResponseHolder;

    private boolean status;
    private ValidatePaymentRequest validatePaymentRequest;
    private HashMap<String, String> paramsMap;
    private static String PAYMENT_REQUEST = "payment_request";
    private static String PAYMENT_STATUS = "payment_status";
    private static String ADDITIONAL_PARAMS = "additional_params";

    private PaytmResponseHolder(boolean status, ValidatePaymentRequest validatePaymentRequest,
                                HashMap<String, String> paramsMap) {
        this.status = status;
        this.validatePaymentRequest = validatePaymentRequest;
        this.paramsMap = paramsMap;
    }

    public static void setPaytmResponse(boolean status, ValidatePaymentRequest validatePaymentRequest,
                                        HashMap<String, String> paramsMap) {
        paytmResponseHolder = new PaytmResponseHolder(status, validatePaymentRequest, paramsMap);
    }

    public static synchronized boolean hasPendingTransaction() {
        return paytmResponseHolder != null && paytmResponseHolder.validatePaymentRequest != null;
    }

    public static <T extends AppOperationAware> void processPaytmResponse(T ctx) {
        boolean status ;
        ValidatePaymentRequest validateRequest;
        HashMap<String, String> params ;
        synchronized (PaytmResponseHolder.class){
            status = paytmResponseHolder.status;
            params = paytmResponseHolder.paramsMap;
            validateRequest = paytmResponseHolder.validatePaymentRequest;

        }
        new ValidatePayment<>(ctx, validateRequest).validatePaytm(status, params);
        synchronized (PaytmResponseHolder.class) {
            paytmResponseHolder = null;
        }
    }

    public static <T extends AppOperationAware> void processPaytmResponse(T ctx, BigBasketRetryMessageHandler handler) {
        boolean status ;
        ValidatePaymentRequest validateRequest;
        HashMap<String, String> params ;
        synchronized (PaytmResponseHolder.class){
            status = paytmResponseHolder.status;
            params = paytmResponseHolder.paramsMap;
            validateRequest = paytmResponseHolder.validatePaymentRequest;

        }
        //saving the parameters in handler to be used in case of retry of validatepayment
        Bundle bundle = new Bundle(3);
        bundle.putParcelable(PAYMENT_REQUEST, validateRequest);
        bundle.putBoolean(PAYMENT_STATUS, status);
        Gson gson = new Gson();
        String jsonPaymentParams = gson.toJson(params);
        bundle.putString(ADDITIONAL_PARAMS, jsonPaymentParams);
        handler.bundleData = bundle;

        new ValidatePayment<>(ctx, validateRequest, handler).validatePaytm(status, params);
        synchronized (PaytmResponseHolder.class) {
            paytmResponseHolder = null;
        }
    }

    //this method is used to validate the paytm response in case of retry
    public static <T extends AppOperationAware> void processPaytmRetryResponse(T ctx, BigBasketRetryMessageHandler handler, Bundle bundleData) {
        try {
            Gson gson = new Gson();
            Type stringStringMap = new TypeToken<HashMap<String, String>>() {
            }.getType();
            HashMap<String, String> mPaymentParams = gson.fromJson(bundleData.getString(ADDITIONAL_PARAMS), stringStringMap);
            new ValidatePayment<>(ctx, (ValidatePaymentRequest) bundleData.getParcelable(PAYMENT_REQUEST), handler)
                    .validatePaytm(bundleData.getBoolean(PAYMENT_STATUS), mPaymentParams);
        } catch (Exception e) {
            Crashlytics.logException(new ClassCastException(
                    "Exception while getting values from bundle"));
        }
    }
}
