package com.bigbasket.mobileapp.task;

import android.content.Intent;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.PaymentSelectionActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.request.SelectedShipment;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostShipmentResponseContent;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.payment.PaymentWalletChangeListener;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;

public class PostShipmentTask<T extends AppOperationAware> {

    public static <T extends AppOperationAware> void startTask(final T ctx, final ArrayList<SelectedShipment> selectedShipments,
                                                               final String potentialOrderId,
                                                               final String nc) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(
                ctx.getCurrentActivity());
        ctx.showProgressDialog(ctx.getCurrentActivity().getResources().getString(R.string.please_wait));
        final String selectedShipmentsJsonString = new Gson().toJson(selectedShipments);
        Call<ApiResponse<PostShipmentResponseContent>> call =
                bigBasketApiService.postShipment(ctx.getCurrentActivity().getPreviousScreenName(), selectedShipmentsJsonString,
                        potentialOrderId, "yes", "yes", "yes", "yes", "yes");
        call.enqueue(new BBNetworkCallback<ApiResponse<PostShipmentResponseContent>>(ctx, true) {
            @Override
            public void onSuccess(ApiResponse<PostShipmentResponseContent> postShipmentResponse) {

                switch (postShipmentResponse.status) {
                    case 0:
                        onPostShipment(ctx, potentialOrderId, nc, postShipmentResponse.apiResponseContent, selectedShipmentsJsonString);
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

    private static <T extends AppOperationAware> void onPostShipment(T ctx, String potentialOrderId,
                                                                     String nc,
                                                                     PostShipmentResponseContent postShipmentResponseContent,
                                                                     String selectedShipmentsJsonString) {
        Intent intent = new Intent(ctx.getCurrentActivity(),
                PaymentSelectionActivity.class);
        intent.putExtra(Constants.P_ORDER_ID, potentialOrderId);
        intent.putExtra(Constants.SHIPMENTS, selectedShipmentsJsonString);
        intent.putParcelableArrayListExtra(Constants.PAYMENT_TYPES,
                postShipmentResponseContent.paymentTypes);
        intent.putParcelableArrayListExtra(Constants.CREDIT_DETAILS,
                postShipmentResponseContent.creditDetails);
        intent.putExtra(Constants.HAS_GIFTS,
                ctx.getCurrentActivity().getIntent().getBooleanExtra(Constants.HAS_GIFTS, false));
        intent.putExtra(Constants.ORDER_DETAILS, postShipmentResponseContent.orderDetails);
        intent.putExtra(Constants.EVOUCHER_CODE, postShipmentResponseContent.evoucherCode);
        intent.putExtra(Constants.NEW_FLOW_URL, postShipmentResponseContent.newFlowUrl);
        intent.putExtra(Constants.WALLET_OPTION, postShipmentResponseContent.walletOption);
        intent.putParcelableArrayListExtra(Constants.VOUCHERS,
                postShipmentResponseContent.activeVouchersArrayList);
        ctx.getCurrentActivity().setCurrentScreenName(nc);
        ctx.getCurrentActivity().startActivityForResult(intent,
                NavigationCodes.GO_TO_HOME);
    }

    public static <T extends AppOperationAware & PaymentWalletChangeListener> void startTaskWalletUpdate(final T ctx, String selectedShipments,
                                                                                                         final String potentialOrderId,
                                                                                                         final String nc, int wallet) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(
                ctx.getCurrentActivity());
        ctx.showProgressDialog(ctx.getCurrentActivity().getResources().getString(R.string.please_wait));
        Call<ApiResponse<PostShipmentResponseContent>> call =
                bigBasketApiService.postShipment(ctx.getCurrentActivity().getPreviousScreenName(), selectedShipments, wallet,
                        potentialOrderId, "yes", "yes", "yes", "yes", "yes");
        call.enqueue(new BBNetworkCallback<ApiResponse<PostShipmentResponseContent>>(ctx, true) {
            @Override
            public void onSuccess(ApiResponse<PostShipmentResponseContent> postShipmentResponse) {

                switch (postShipmentResponse.status) {
                    case 0:
                        ((PaymentWalletChangeListener) ctx).paymentWalletOptionChanged(postShipmentResponse.apiResponseContent);
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


}
