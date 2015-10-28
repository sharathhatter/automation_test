package com.bigbasket.mobileapp.task;

import android.content.Intent;

import com.bigbasket.mobileapp.activity.order.uiv3.PaymentSelectionActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.request.SelectedShipment;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostShipmentResponseContent;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PostShipmentTask<T> {
    private T ctx;
    private ArrayList<SelectedShipment> selectedShipments;
    private String potentialOrderId;
    private String nc;

    public PostShipmentTask(T ctx, ArrayList<SelectedShipment> selectedShipments, String potentialOrderId,
                            String nc) {
        this.ctx = ctx;
        this.selectedShipments = selectedShipments;
        this.potentialOrderId = potentialOrderId;
        this.nc = nc;
    }

    public void startTask() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(
                ((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        bigBasketApiService.postShipment(new Gson().toJson(selectedShipments), potentialOrderId, "yes", "yes", "yes", "yes",
                new Callback<ApiResponse<PostShipmentResponseContent>>() {
                    @Override
                    public void success(ApiResponse<PostShipmentResponseContent> postShipmentResponse, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (postShipmentResponse.status) {
                            case 0:
                                onPostShipment(postShipmentResponse.apiResponseContent);
                                break;
                            default:
                                ((HandlerAware) ctx).getHandler().sendEmptyMessage(postShipmentResponse.status,
                                        postShipmentResponse.message, true);
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
                        ((HandlerAware) ctx).getHandler().handleRetrofitError(error, true);
                    }
                });
    }

    private void onPostShipment(PostShipmentResponseContent postShipmentResponseContent) {
        Intent intent = new Intent(((ActivityAware) ctx).getCurrentActivity(),
                PaymentSelectionActivity.class);
        intent.putExtra(Constants.P_ORDER_ID, potentialOrderId);
        intent.putParcelableArrayListExtra(Constants.PAYMENT_TYPES,
                postShipmentResponseContent.paymentTypes);
        intent.putParcelableArrayListExtra(Constants.CREDIT_DETAILS,
                postShipmentResponseContent.creditDetails);
        intent.putExtra(Constants.HAS_GIFTS,
                ((ActivityAware) ctx).getCurrentActivity().getIntent().getBooleanExtra(Constants.HAS_GIFTS, false));
        intent.putExtra(Constants.ORDER_DETAILS, postShipmentResponseContent.orderDetails);
        intent.putExtra(Constants.EVOUCHER_CODE, postShipmentResponseContent.evoucherCode);
        intent.putExtra(Constants.NEW_FLOW_URL, postShipmentResponseContent.newFlowUrl);
        intent.putParcelableArrayListExtra(Constants.VOUCHERS,
                postShipmentResponseContent.activeVouchersArrayList);
        ((ActivityAware) ctx).getCurrentActivity().setNextScreenNavigationContext(nc);
        ((ActivityAware) ctx).getCurrentActivity().startActivityForResult(intent,
                NavigationCodes.GO_TO_HOME);
    }
}
