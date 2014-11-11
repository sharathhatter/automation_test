package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.order.COReserveQuantity;

public interface COReserveQuantityCheckAware {
    COReserveQuantity getCOReserveQuantity();

    void setCOReserveQuantity(COReserveQuantity coReserveQuantity);

    void onCOReserveQuantityCheck();
}
