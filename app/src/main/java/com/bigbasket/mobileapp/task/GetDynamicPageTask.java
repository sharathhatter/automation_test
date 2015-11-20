package com.bigbasket.mobileapp.task;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDynamicPageApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.DynamicScreenAware;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionUtil;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;

import retrofit.Call;

public class GetDynamicPageTask<T extends AppOperationAware & DynamicScreenAware> {
    private T context;
    private String screenName;
    private boolean inlineProgress;
    private boolean finishOnError;

    public GetDynamicPageTask(T context, String screenName, boolean inlineProgress,
                              boolean finishOnError) {
        this.context = context;
        this.screenName = screenName;
        this.inlineProgress = inlineProgress;
        this.finishOnError = finishOnError;
    }

    public void startTask() {
        if (!context.checkInternetConnection()) {
            context.getHandler().sendOfflineError(finishOnError);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(context.getCurrentActivity());
        if (inlineProgress) {
            context.showProgressView();
        } else {
            context.showProgressDialog("Please wait...");
        }
        Call<ApiResponse<GetDynamicPageApiResponse>> call = bigBasketApiService.getDynamicPage(Constants.ANDROID,
                DataUtil.getAppVersion(context.getCurrentActivity()),
                screenName);
        call.enqueue(new DynamicPageCallback(context));
    }

    private class DynamicPageCallback extends BBNetworkCallback<ApiResponse<GetDynamicPageApiResponse>> {

        private DynamicPageCallback(T ctx) {
            super(ctx);
        }

        @Override
        public void onSuccess(ApiResponse<GetDynamicPageApiResponse> getDynamicPageApiResponse) {
            switch (getDynamicPageApiResponse.status) {
                case 0:
                    SectionData sectionData = getDynamicPageApiResponse.apiResponseContent.sectionData;
                    if (sectionData != null) {
                        sectionData.setSections(SectionUtil.preserveMemory(sectionData.getSections()));
                    }
                    context.onDynamicScreenSuccess(screenName, sectionData);
                    break;
                default:
                    context.onDynamicScreenFailure(getDynamicPageApiResponse.status,
                                getDynamicPageApiResponse.message);
                    break;
            }
        }

        @Override
        public boolean updateProgress() {
            try {
                if (inlineProgress) {
                    context.hideProgressView();
                } else {
                    context.hideProgressDialog();
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
            return true;
        }

        @Override
        public void onFailure(int httpErrorCode, String msg) {
            context.onDynamicScreenHttpFailure(httpErrorCode, msg);
        }

        @Override
        public void onFailure(Throwable t) {
            context.onDynamicScreenFailure(t);
        }
    }
}
