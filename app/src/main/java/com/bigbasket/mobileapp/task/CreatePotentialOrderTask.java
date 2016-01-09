package com.bigbasket.mobileapp.task;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.CreatePotentialOrderAware;

import retrofit2.Call;

public class CreatePotentialOrderTask<T extends AppOperationAware> {
    private T ctx;
    private String addressId;

    public CreatePotentialOrderTask(T ctx, String addressId) {
        this.ctx = ctx;
        this.addressId = addressId;
    }

    public void startTask() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(ctx.getCurrentActivity());
        ctx.showProgressDialog(ctx.getCurrentActivity().getString(R.string.checkingAvailability),
                false);
        Call<ApiResponse<CreatePotentialOrderResponseContent>> call =
                bigBasketApiService.createPotentialOrder(ctx.getCurrentActivity().getPreviousScreenName(), addressId);
        call.enqueue(new BBNetworkCallback<ApiResponse<CreatePotentialOrderResponseContent>>(ctx, true) {
            @Override
            public void onSuccess(ApiResponse<CreatePotentialOrderResponseContent> createPotentialOrderApiResponse) {
                switch (createPotentialOrderApiResponse.status) {
                    case 0:
                        ((CreatePotentialOrderAware) ctx).onPotentialOrderCreated(createPotentialOrderApiResponse.apiResponseContent);
                        break;
                    default:
                        ctx.getHandler().sendEmptyMessage(createPotentialOrderApiResponse.status,
                                createPotentialOrderApiResponse.message, true);
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
