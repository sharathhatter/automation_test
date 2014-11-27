package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.account.Address;

public interface AddressSelectionAware {
    public void onAddressSelected(Address address);
}