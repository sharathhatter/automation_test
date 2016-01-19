package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.account.Address;

public interface AddressChangeAware {

    void onAddNewAddressClicked();

    void onEditAddressClicked(Address address);
}
