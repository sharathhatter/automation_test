package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.order.QCErrorData;

import java.util.ArrayList;

public interface OnAddressChangeListener {
    void onAddressChanged(ArrayList<AddressSummary> addressSummaries);
    void onAddressNotSupported(String msg);
    void onBasketDelta(String addressId, String title, String msg,
                       boolean hasQcError, ArrayList<QCErrorData> qcErrorDatas);
    void onNoBasketDelta(String addressId);
}
