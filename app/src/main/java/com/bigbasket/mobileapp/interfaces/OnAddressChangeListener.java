package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.order.QCErrorData;

import java.util.ArrayList;

public interface OnAddressChangeListener {

    void onAddressChanged(ArrayList<AddressSummary> addressSummaries, @Nullable String selectedAddressId);

    void onAddressNotSupported(String msg);

}
