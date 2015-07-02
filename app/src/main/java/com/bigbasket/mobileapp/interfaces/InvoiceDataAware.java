package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.order.OrderInvoice;

public interface InvoiceDataAware {

    void onDisplayOrderInvoice(OrderInvoice orderInvoice);
}
