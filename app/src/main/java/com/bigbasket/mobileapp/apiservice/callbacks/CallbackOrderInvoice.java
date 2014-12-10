package com.bigbasket.mobileapp.apiservice.callbacks;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CallbackOrderInvoice<T> implements Callback<ApiResponse<OrderInvoice>> {

    private T ctx;

    public CallbackOrderInvoice(T ctx) {
        this.ctx = ctx;
    }

    @Override
    public void success(ApiResponse<OrderInvoice> orderInvoiceApiResponse, Response response) {
        if (((CancelableAware) ctx).isSuspended()) {
            return;
        } else {
            try {
                ((ProgressIndicationAware) ctx).hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        switch (orderInvoiceApiResponse.status) {
            case 0:
                ((InvoiceDataAware) ctx).onDisplayOrderInvoice(orderInvoiceApiResponse.apiResponseContent);
                break;
            default:
                ((HandlerAware) ctx).getHandler().sendEmptyMessage(orderInvoiceApiResponse.status);
                break;
        }
    }

    @Override
    public void failure(RetrofitError error) {
        if (((CancelableAware) ctx).isSuspended()) {
            return;
        } else {
            try {
                ((ProgressIndicationAware) ctx).hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
    }
}
