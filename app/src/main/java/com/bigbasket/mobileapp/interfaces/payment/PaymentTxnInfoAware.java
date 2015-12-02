package com.bigbasket.mobileapp.interfaces.payment;

public interface PaymentTxnInfoAware {
    void setTxnDetails(String txnId, String amount);
}
