package com.bigbasket.mobileapp.task;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.CreatePotentialOrderAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreatePotentialOrderTask<T> {
    private T ctx;
    private String addressId;

    public CreatePotentialOrderTask(T ctx, String addressId) {
        this.ctx = ctx;
        this.addressId = addressId;
    }

    public void startTask() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog(((ActivityAware) ctx).getCurrentActivity().getString(R.string.checkingAvailability),
                false);
        bigBasketApiService.createPotentialOrder(addressId, "yes", "yes", new Callback<ApiResponse<CreatePotentialOrderResponseContent>>() {
            @Override
            public void success(ApiResponse<CreatePotentialOrderResponseContent> createPotentialOrderApiResponse, Response response) {
                if (((CancelableAware) ctx).isSuspended()) return;
                try {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (createPotentialOrderApiResponse.status) {
                    case 0:
                        ((CreatePotentialOrderAware) ctx).onPotentialOrderCreated(createPotentialOrderApiResponse.apiResponseContent);
                        break;
                    default:
                        ((HandlerAware) ctx).getHandler().sendEmptyMessage(createPotentialOrderApiResponse.status,
                                createPotentialOrderApiResponse.message, true);
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
}
