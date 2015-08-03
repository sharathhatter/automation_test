package com.bigbasket.mobileapp.interfaces.payment;

import com.bigbasket.mobileapp.model.order.PowerPayPostParams;

public interface PowerPayPaymentAware {
    void initializeHDFCPowerPay(PowerPayPostParams powerPayPostParams);
}
