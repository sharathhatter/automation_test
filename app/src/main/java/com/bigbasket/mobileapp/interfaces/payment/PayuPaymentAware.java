package com.bigbasket.mobileapp.interfaces.payment;

import java.util.HashMap;

public interface PayuPaymentAware {
    void initializePayu(HashMap<String, String> paymentParams);
}
