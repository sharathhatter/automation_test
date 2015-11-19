package com.bigbasket.mobileapp.task.uiv3;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SetAddressResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SetAddressTransientResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.OnAddressChangeListener;
import com.bigbasket.mobileapp.util.ApiErrorCodes;

import retrofit.Call;

public class ChangeAddressTask<T extends OnAddressChangeListener & AppOperationAware> {
    private T ctx;
    @Nullable
    private String addressId;
    @Nullable
    private String lat;
    @Nullable
    private String lng;
    private boolean isTransient;
    @Nullable
    private String area;

    public ChangeAddressTask(T ctx,
                             @Nullable String addressId,
                             @Nullable String lat,
                             @Nullable String lng,
                             @Nullable String area,
                             boolean isTransient) {
        this.ctx = ctx;
        this.addressId = addressId;
        this.lat = lat;
        this.lng = lng;
        this.isTransient = isTransient;
        this.area = area;
    }

    public void startTask() {
        if (!ctx.checkInternetConnection()) {
            ctx.getHandler().sendOfflineError();
        }

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter
                .getApiService(ctx.getCurrentActivity());
        if (isTransient) {
            ctx.showProgressDialog("Checking for changes in basket...");
            Call<ApiResponse<SetAddressTransientResponse>> call =
                    bigBasketApiService.setCurrentAddress(addressId, lat, lng, isTransient ? "1" : "0", area);
            call.enqueue(new BBNetworkCallback<ApiResponse<SetAddressTransientResponse>>(ctx) {
                @Override
                public void onSuccess(ApiResponse<SetAddressTransientResponse> setAddressTransientResponse) {
                    switch (setAddressTransientResponse.status) {
                        case 0:
                            if (!TextUtils.isEmpty(setAddressTransientResponse.apiResponseContent.title) ||
                                    !TextUtils.isEmpty(setAddressTransientResponse.apiResponseContent.msg) ||
                                    setAddressTransientResponse.apiResponseContent.hasQcErrors ||
                                    (setAddressTransientResponse.apiResponseContent.qcErrorDatas != null
                                            && setAddressTransientResponse.apiResponseContent.qcErrorDatas.size() > 0)) {
                                ctx.onBasketDelta(addressId, lat, lng,
                                        setAddressTransientResponse.apiResponseContent.title,
                                        setAddressTransientResponse.apiResponseContent.msg,
                                        area,
                                        setAddressTransientResponse.apiResponseContent.hasQcErrors,
                                        setAddressTransientResponse.apiResponseContent.qcErrorDatas);
                            } else {
                                ctx.onNoBasketDelta(addressId, lat, lng, area);
                            }
                            break;
                        default:
                            ctx.getHandler().sendEmptyMessage(setAddressTransientResponse.status,
                                    setAddressTransientResponse.message);
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
        } else {
            ctx.showProgressDialog("Updating your address...");
            Call<ApiResponse<SetAddressResponse>> call = bigBasketApiService.setCurrentAddress(addressId, lat, lng, area);
            call.enqueue(new BBNetworkCallback<ApiResponse<SetAddressResponse>>(ctx) {
                @Override
                public void onSuccess(ApiResponse<SetAddressResponse> getAddressSummaryApiResponse) {
                    switch (getAddressSummaryApiResponse.status) {
                        case 0:
                            ctx.onAddressChanged(getAddressSummaryApiResponse.apiResponseContent.addressSummaries);
                            break;
                        case ApiErrorCodes.ADDRESS_NOT_SERVED:
                            ctx.onAddressNotSupported(getAddressSummaryApiResponse.message);
                            break;
                        default:
                            ctx.getHandler().sendEmptyMessage(getAddressSummaryApiResponse.status,
                                    getAddressSummaryApiResponse.message);
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
}
