package com.bigbasket.mobileapp.interfaces.location;

import android.location.Address;
import android.support.annotation.Nullable;

import java.util.List;

public interface OnAddressFetchedListener {
    void onAddressFetched(@Nullable List<Address> addresses);
}
