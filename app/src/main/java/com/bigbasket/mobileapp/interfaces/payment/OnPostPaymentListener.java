package com.bigbasket.mobileapp.interfaces.payment;

public interface OnPostPaymentListener {
    void onPostPaymentSuccess(String txnId, String paymentType);

    void onPostPaymentFailure(String txnId, String paymentType);
}
