package com.bigbasket.mobileapp.task.uiv3;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetAddressSummaryResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.OnAddressChangeListener;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.util.ApiErrorCodes;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChangeAddressTask<T extends OnAddressChangeListener> {
    private T ctx;
    @Nullable
    private String addressId;
    @Nullable
    private String lat;
    @Nullable
    private String lng;

    public ChangeAddressTask(T ctx,
                             @Nullable String addressId,
                             @Nullable String lat,
                             @Nullable String lng) {
        this.ctx = ctx;
        this.addressId = addressId;
        this.lat = lat;
        this.lng = lng;
    }

    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter
                .getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
        bigBasketApiService.setCurrentAddress(addressId, lat, lng, new Callback<ApiResponse<GetAddressSummaryResponse>>() {
            @Override
            public void success(ApiResponse<GetAddressSummaryResponse> getAddressSummaryApiResponse, Response response) {
                if (((CancelableAware) ctx).isSuspended()) return;
                try {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (getAddressSummaryApiResponse.status) {
                    case 0:
                        ctx.onAddressChanged(getAddressSummaryApiResponse.apiResponseContent.addressSummaries);
                        break;
                    case ApiErrorCodes.ADDRESS_NOT_SERVED:
                        ctx.onAddressNotSupported(getAddressSummaryApiResponse.message);
                        break;
                    default:
                        ((HandlerAware) ctx).getHandler().sendEmptyMessage(getAddressSummaryApiResponse.status,
                                getAddressSummaryApiResponse.message);
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
    }
}
