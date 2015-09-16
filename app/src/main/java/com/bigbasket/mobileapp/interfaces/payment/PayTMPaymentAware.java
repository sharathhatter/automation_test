package com.bigbasket.mobileapp.interfaces.payment;

import java.util.HashMap;

/**
 * Created by manu on 14/9/15.
 */
public interface PayTMPaymentAware {
    void initializePayTm(HashMap<String, String> paymentParams);
}
