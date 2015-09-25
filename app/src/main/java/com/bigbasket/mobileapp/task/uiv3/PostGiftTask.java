package com.bigbasket.mobileapp.task.uiv3;

import android.content.Intent;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.activity.order.uiv3.ShipmentSelectionActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostGiftItemsResponseContent;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PostGiftTask<T> {
    private T ctx;
    private String potentialOrderId;
    private ArrayList<GiftItem> gifts;
    private String nc;

    public PostGiftTask(T ctx, String potentialOrderId, @Nullable ArrayList<GiftItem> gifts,
                        String nc) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.nc = nc;
        this.gifts = gifts;
    }

    public void startTask() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(
                ((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        String giftsJson = gifts != null ? new Gson().toJson(gifts) : null;
        bigBasketApiService.postGifts(potentialOrderId, giftsJson,
                new Callback<ApiResponse<PostGiftItemsResponseContent>>() {
                    @Override
                    public void success(ApiResponse<PostGiftItemsResponseContent> postGiftItemsResponseContent, Response response) {
                        if (((CancelableAware) ctx).isSuspended()) return;
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (postGiftItemsResponseContent.status) {
                            case 0:
                                onPostGifts(postGiftItemsResponseContent.apiResponseContent);
                                break;
                            default:
                                ((HandlerAware) ctx).getHandler().sendEmptyMessage(postGiftItemsResponseContent.status,
                                        postGiftItemsResponseContent.message, true);
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

    private void onPostGifts(PostGiftItemsResponseContent postGiftItemsResponseContent) {
        Intent intent = new Intent(((ActivityAware) ctx).getCurrentActivity(),
                ShipmentSelectionActivity.class);
        intent.putParcelableArrayListExtra(Constants.SHIPMENTS, postGiftItemsResponseContent.shipments);
        intent.putExtra(Constants.ORDER_DETAILS, postGiftItemsResponseContent.orderDetails);
        intent.putExtra(Constants.P_ORDER_ID, potentialOrderId);
        if (postGiftItemsResponseContent.defaultShipmentActions != null) {
            intent.putExtra(Constants.DEFAULT_ACTIONS,
                    new Gson().toJson(postGiftItemsResponseContent.defaultShipmentActions));
        }
        if (postGiftItemsResponseContent.toggleShipmentActions != null) {
            intent.putExtra(Constants.ON_TOGGLE_ACTIONS,
                    new Gson().toJson(postGiftItemsResponseContent.toggleShipmentActions));
        }
        ((ActivityAware) ctx).getCurrentActivity().setNextScreenNavigationContext(nc);
        ((ActivityAware) ctx).getCurrentActivity().startActivityForResult(intent,
                NavigationCodes.GO_TO_HOME);
    }
}

