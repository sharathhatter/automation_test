package com.bigbasket.mobileapp.task;

import android.content.Intent;

import com.bigbasket.mobileapp.activity.order.uiv3.PaymentSelectionActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.request.SelectedShipment;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostShipmentResponseContent;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit.Call;

public class PostShipmentTask<T extends AppOperationAware> {
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
                ctx.getCurrentActivity());
        ctx.showProgressDialog("Please wait...");
        Call<ApiResponse<PostShipmentResponseContent>> call =
                bigBasketApiService.postShipment(new Gson().toJson(selectedShipments),
                        potentialOrderId, "yes", "yes", "yes", "yes", "yes");
        call.enqueue(new BBNetworkCallback<ApiResponse<PostShipmentResponseContent>>(ctx, true) {
            @Override
            public void onSuccess(ApiResponse<PostShipmentResponseContent> postShipmentResponse) {

                switch (postShipmentResponse.status) {
                    case 0:
                        onPostShipment(postShipmentResponse.apiResponseContent);
                        break;
                    default:
                        ctx.getHandler().sendEmptyMessage(postShipmentResponse.status,
                                postShipmentResponse.message, true);
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
        });
    }

    private void onPostShipment(PostShipmentResponseContent postShipmentResponseContent) {
        Intent intent = new Intent(ctx.getCurrentActivity(),
                PaymentSelectionActivity.class);
        intent.putExtra(Constants.P_ORDER_ID, potentialOrderId);
        intent.putParcelableArrayListExtra(Constants.PAYMENT_TYPES,
                postShipmentResponseContent.paymentTypes);
        intent.putParcelableArrayListExtra(Constants.CREDIT_DETAILS,
                postShipmentResponseContent.creditDetails);
        intent.putExtra(Constants.HAS_GIFTS,
                ctx.getCurrentActivity().getIntent().getBooleanExtra(Constants.HAS_GIFTS, false));
        intent.putExtra(Constants.ORDER_DETAILS, postShipmentResponseContent.orderDetails);
        intent.putExtra(Constants.EVOUCHER_CODE, postShipmentResponseContent.evoucherCode);
        intent.putExtra(Constants.NEW_FLOW_URL, postShipmentResponseContent.newFlowUrl);
        intent.putParcelableArrayListExtra(Constants.VOUCHERS,
                postShipmentResponseContent.activeVouchersArrayList);
        ctx.getCurrentActivity().setNextScreenNavigationContext(nc);
        ctx.getCurrentActivity().startActivityForResult(intent,
                NavigationCodes.GO_TO_HOME);
    }
}
