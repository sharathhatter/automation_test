package com.bigbasket.mobileapp.interfaces.payment;

public interface OnPostPaymentListener {
    void onPostPaymentSuccess(String txnId);
    void onPostPaymentFailure(String txnId);
}
