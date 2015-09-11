package com.bigbasket.mobileapp.task.uiv3;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SetAddressResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SetAddressTransientResponse;
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
    private boolean isTransient;

    public ChangeAddressTask(T ctx,
                             @Nullable String addressId,
                             @Nullable String lat,
                             @Nullable String lng,
                             boolean isTransient) {
        this.ctx = ctx;
        this.addressId = addressId;
        this.lat = lat;
        this.lng = lng;
        this.isTransient = isTransient;
    }

    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter
                .getApiService(((ActivityAware) ctx).getCurrentActivity());
        if (isTransient) {
            ((ProgressIndicationAware) ctx).showProgressDialog("Checking for changes in basket...");
            bigBasketApiService.setCurrentAddress(addressId, lat, lng, isTransient ? "1" : "0",
                    new Callback<ApiResponse<SetAddressTransientResponse>>() {
                        @Override
                        public void success(ApiResponse<SetAddressTransientResponse> setAddressTransientResponse, Response response) {
                            if (((CancelableAware) ctx).isSuspended()) return;
                            try {
                                ((ProgressIndicationAware) ctx).hideProgressDialog();
                            } catch (IllegalArgumentException e) {
                                return;
                            }
                            switch (setAddressTransientResponse.status) {
                                case 0:
                                    if (!TextUtils.isEmpty(setAddressTransientResponse.apiResponseContent.title) ||
                                            !TextUtils.isEmpty(setAddressTransientResponse.apiResponseContent.msg) ||
                                            setAddressTransientResponse.apiResponseContent.hasQcErrors ||
                                            (setAddressTransientResponse.apiResponseContent.qcErrorDatas != null
                                                    && setAddressTransientResponse.apiResponseContent.qcErrorDatas.size() > 0)) {
                                        ctx.onBasketDelta(addressId, setAddressTransientResponse.apiResponseContent.title,
                                                setAddressTransientResponse.apiResponseContent.msg,
                                                setAddressTransientResponse.apiResponseContent.hasQcErrors,
                                                setAddressTransientResponse.apiResponseContent.qcErrorDatas);
                                    } else {
                                        ctx.onNoBasketDelta(addressId);
                                    }
                                    break;
                                default:
                                    ((HandlerAware) ctx).getHandler().sendEmptyMessage(setAddressTransientResponse.status,
                                            setAddressTransientResponse.message);
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
        } else {
            ((ProgressIndicationAware) ctx).showProgressDialog("Updating your address...");
            bigBasketApiService.setCurrentAddress(addressId, lat, lng, new Callback<ApiResponse<SetAddressResponse>>() {
                @Override
                public void success(ApiResponse<SetAddressResponse> getAddressSummaryApiResponse, Response response) {
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
}
