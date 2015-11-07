package com.bigbasket.mobileapp.apiservice.callbacks;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;

public class CallbackOrderInvoice<T extends AppOperationAware> extends BBNetworkCallback<ApiResponse<OrderInvoice>> {

    private T ctx;

    public CallbackOrderInvoice(T ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    @Override
    public void onSuccess(ApiResponse<OrderInvoice> orderInvoiceApiResponse) {

        switch (orderInvoiceApiResponse.status) {
            case 0:
                ((InvoiceDataAware) ctx).onDisplayOrderInvoice(orderInvoiceApiResponse.apiResponseContent);
                break;
            default:
                ctx.getHandler().sendEmptyMessage(orderInvoiceApiResponse.status,
                        orderInvoiceApiResponse.message);
                break;
        }
    }

    @Override
    public boolean updateProgress() {
        try {
            ctx.hideProgressDialog();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
