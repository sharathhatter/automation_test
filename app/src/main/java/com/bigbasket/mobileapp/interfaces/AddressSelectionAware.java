package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.account.Address;

public interface AddressSelectionAware {

    void onAddressSelected(Address address);

    @Nullable
    Address getSelectedAddress();
}
