package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.account.AddressSummary;

import java.util.ArrayList;

public interface OnAddressChangeListener {
    void onAddressChanged(ArrayList<AddressSummary> addressSummaries);
    void onAddressNotSupported(String msg);
}
