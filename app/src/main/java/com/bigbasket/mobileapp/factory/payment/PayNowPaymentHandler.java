package com.bigbasket.mobileapp.factory.payment;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPayzappPaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPrepaidPaymentResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.util.Constants;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PayNowPaymentHandler<T> extends PaymentHandler<T> {

    public PayNowPaymentHandler(T ctx, String potentialOrderId, String orderId,
                                String paymentMethod, boolean isPayNow, boolean isFundWallet) {
        super(ctx, potentialOrderId, orderId, paymentMethod, isPayNow, isFundWallet);
    }

    @Override
    public void initiate() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        switch (paymentMethod) {
            case Constants.HDFC_POWER_PAY:
                bigBasketApiService.postPayzappPayNowDetails(orderId, paymentMethod, new Callback<ApiResponse<GetPayzappPaymentParamsResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPayzappPaymentParamsResponse> getPayzappPaymentParamsApiResponse, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (getPayzappPaymentParamsApiResponse.status) {
                            case 0:
                                openPayzappGateway(getPayzappPaymentParamsApiResponse.apiResponseContent.payzappPostParams);
                                break;
                            default:
                                ((HandlerAware) ctx).getHandler().sendEmptyMessage(getPayzappPaymentParamsApiResponse.status,
                                        getPayzappPaymentParamsApiResponse.message);
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
                    }
                });
                break;
            default:
                bigBasketApiService.postPayNowDetails(orderId, paymentMethod, new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPrepaidPaymentResponse> getPrepaidPaymentApiResponse, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                openGateway(getPrepaidPaymentApiResponse.apiResponseContent.postParams);
                                break;
                            default:
                                ((HandlerAware) ctx).getHandler().sendEmptyMessage(getPrepaidPaymentApiResponse.status,
                                        getPrepaidPaymentApiResponse.message);
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
                    }
                });
                break;
        }
    }

}
