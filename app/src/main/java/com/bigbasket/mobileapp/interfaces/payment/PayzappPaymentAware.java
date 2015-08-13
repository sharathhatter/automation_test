package com.bigbasket.mobileapp.interfaces.payment;

import com.bigbasket.mobileapp.model.order.PayzappPostParams;

public interface PayzappPaymentAware {
    void initializeHDFCPayzapp(PayzappPostParams payzappPostParams);
}
