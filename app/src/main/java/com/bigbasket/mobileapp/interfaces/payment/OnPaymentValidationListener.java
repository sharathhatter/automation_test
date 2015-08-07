package com.bigbasket.mobileapp.interfaces.payment;

import android.support.annotation.Nullable;

public interface OnPaymentValidationListener {
    void onPaymentValidated(boolean status, @Nullable String msg);
}
