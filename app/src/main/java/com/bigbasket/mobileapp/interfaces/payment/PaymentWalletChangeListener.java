package com.bigbasket.mobileapp.interfaces.payment;

import com.bigbasket.mobileapp.apiservice.models.response.PostShipmentResponseContent;

/**
 * Created by manu on 29/2/16.
 */
public interface PaymentWalletChangeListener {

    void paymentWalletOptionChanged(PostShipmentResponseContent postShipmentResponseContent);
}
