package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.order.QCErrorData;

import java.util.ArrayList;

public interface OnAddressChangeListener {
    void onAddressChanged(ArrayList<AddressSummary> addressSummaries);

    void onAddressNotSupported(String msg);

    void onBasketDelta(String addressId, String lat, String lng,
                       String title, String msg,
                       @Nullable String area,
                       boolean hasQcError, ArrayList<QCErrorData> qcErrorDatas);

    void onNoBasketDelta(String addressId, String lat, String lng, @Nullable String area);
}
