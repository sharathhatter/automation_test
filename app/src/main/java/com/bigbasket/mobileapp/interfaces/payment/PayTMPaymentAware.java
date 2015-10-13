package com.bigbasket.mobileapp.interfaces.payment;

import java.util.HashMap;


public interface PayTMPaymentAware {
    void initializePayTm(HashMap<String, String> paymentParams);
}
