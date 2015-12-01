package com.bigbasket.mobileapp.interfaces.payment;

/**
 * Created by muniraju on 30/11/15.
 */
public interface PaymentOptionsKnowMoreDialogCallback {
    void onKnowMoreConfirmed(int id, boolean isPositive);
    void onKnowMoreCancelled(int id);
}
