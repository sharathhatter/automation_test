package com.bigbasket.mobileapp.task.uiv3;

import android.content.Intent;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.activity.order.uiv3.ShipmentSelectionActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostGiftItemsResponseContent;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;

public class PostGiftTask<T extends AppOperationAware> {
    private T ctx;
    private String potentialOrderId;
    private Gift gift;
    private String nc;
    private boolean hasGift;

    public PostGiftTask(T ctx, String potentialOrderId, @Nullable Gift gift,
                        String nc) {
        this.ctx = ctx;
        this.potentialOrderId = potentialOrderId;
        this.nc = nc;
        this.gift = gift;
        this.hasGift = gift != null;
    }

    public void startTask() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(
                ctx.getCurrentActivity());
        ctx.showProgressDialog("Please wait...");
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String giftsJson = gift != null ? gson.toJson(gift) : null;
        Call<ApiResponse<PostGiftItemsResponseContent>> call =
                bigBasketApiService.postGifts(ctx.getCurrentActivity().getPreviousScreenName(), potentialOrderId, giftsJson);
        call.enqueue(new BBNetworkCallback<ApiResponse<PostGiftItemsResponseContent>>(ctx, true) {
            @Override
            public void onSuccess(ApiResponse<PostGiftItemsResponseContent> postGiftItemsResponseContent) {
                switch (postGiftItemsResponseContent.status) {
                    case 0:
                        onPostGifts(postGiftItemsResponseContent.apiResponseContent);
                        break;
                    default:
                        ctx.getHandler().sendEmptyMessage(postGiftItemsResponseContent.status,
                                postGiftItemsResponseContent.message, true);
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

    public void setHasGift(boolean hasGift) {
        this.hasGift = hasGift;
    }

    private void onPostGifts(PostGiftItemsResponseContent postGiftItemsResponseContent) {
        Intent intent = new Intent(ctx.getCurrentActivity(),
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
        intent.putExtra(Constants.HAS_GIFTS, hasGift);
        ctx.getCurrentActivity().setCurrentScreenName(nc);
        ctx.getCurrentActivity().startActivityForResult(intent,
                NavigationCodes.GO_TO_HOME);
    }
}

