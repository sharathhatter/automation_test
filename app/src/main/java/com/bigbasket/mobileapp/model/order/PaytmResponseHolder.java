package com.bigbasket.mobileapp.model.order;

import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.factory.payment.ValidatePayment;
import com.bigbasket.mobileapp.handler.BigBasketRetryMessageHandler;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;

import java.util.HashMap;

public class PaytmResponseHolder {
    private static volatile PaytmResponseHolder paytmResponseHolder;

    private boolean status;
    private ValidatePaymentRequest validatePaymentRequest;
    private HashMap<String, String> paramsMap;

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
        new ValidatePayment<>(ctx, paytmResponseHolder.validatePaymentRequest)
                .validatePaytm(paytmResponseHolder.status, paytmResponseHolder.paramsMap);
        paytmResponseHolder = null;
    }
    public static <T extends AppOperationAware> void processPaytmResponse(T ctx, BigBasketRetryMessageHandler handler) {
        new ValidatePayment<>(ctx, paytmResponseHolder.validatePaymentRequest,handler)
                .validatePaytm(paytmResponseHolder.status, paytmResponseHolder.paramsMap);
        paytmResponseHolder = null;
    }
}
