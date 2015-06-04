package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.account.Address;

public interface AddressSelectionAware {
    public void onAddressSelected(Address address);

    public void onAddNewAddressClicked();

    public void onEditAddressClicked(Address address);
}
