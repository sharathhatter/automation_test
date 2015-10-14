package com.bigbasket.mobileapp.task;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDynamicPageApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.DynamicScreenAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.managers.SectionManager;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionUtil;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GetDynamicPageTask<T> {
    private T context;
    private String screenName;
    private boolean inlineProgress;
    private boolean finishOnError;
    private boolean hideProgress;
    private boolean silentMode;

    public GetDynamicPageTask(T context, String screenName, boolean inlineProgress,
                              boolean finishOnError) {
        this.context = context;
        this.screenName = screenName;
        this.inlineProgress = inlineProgress;
        this.finishOnError = finishOnError;
    }

    public GetDynamicPageTask(T context, String screenName, boolean inlineProgress,
                              boolean finishOnError, boolean hideProgress) {
        this(context, screenName, inlineProgress, finishOnError);
        this.hideProgress = hideProgress;
    }

    public GetDynamicPageTask(T context, String screenName, boolean inlineProgress,
                              boolean finishOnError, boolean hideProgress,
                              boolean silentMode) {
        this(context, screenName, inlineProgress, finishOnError, hideProgress);
        this.silentMode = silentMode;
    }

    public void startTask() {
        final SectionManager sectionManager = new SectionManager(((ActivityAware) context).
                getCurrentActivity(), screenName);
        SectionData sectionData = sectionManager.getStoredSectionData();
        if (sectionData != null && sectionData.getSections() != null && sectionData.getSections().size() > 0) {
            ((DynamicScreenAware) context).onDynamicScreenSuccess(screenName, sectionData);
            return;
        }
        if (!((ConnectivityAware) context).checkInternetConnection()) {
            ((HandlerAware) context).getHandler().sendOfflineError(finishOnError);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(((ActivityAware) context).getCurrentActivity());
        if (!silentMode) {
            if (inlineProgress) {
                ((ProgressIndicationAware) context).showProgressView();
            } else {
                ((ProgressIndicationAware) context).showProgressDialog("Please wait...");
            }
        }
        switch (screenName) {
            case SectionManager.HOME_PAGE:
                bigBasketApiService.getHomePage(Constants.ANDROID,
                        DataUtil.getAppVersion(((ActivityAware) context).getCurrentActivity()),
                        new DynamicPageCallback(sectionManager));
                break;
            case SectionManager.MAIN_MENU:
                bigBasketApiService.getMainMenu(Constants.ANDROID,
                        DataUtil.getAppVersion(((ActivityAware) context).getCurrentActivity()),
                        new DynamicPageCallback(sectionManager));
                break;
            default:
                bigBasketApiService.getDynamicPage(Constants.ANDROID,
                        DataUtil.getAppVersion(((ActivityAware) context).getCurrentActivity()),
                        screenName, new DynamicPageCallback(sectionManager));
                break;
        }
    }

    private class DynamicPageCallback implements Callback<ApiResponse<GetDynamicPageApiResponse>> {

        private SectionManager sectionManager;

        private DynamicPageCallback(SectionManager sectionManager) {
            this.sectionManager = sectionManager;
        }

        @Override
        public void success(ApiResponse<GetDynamicPageApiResponse> getDynamicPageApiResponse, Response response) {
            if (((CancelableAware) context).isSuspended()) {
                return;
            }
            if (!hideProgress && !silentMode) {
                try {
                    if (inlineProgress) {
                        ((ProgressIndicationAware) context).hideProgressView();
                    } else {
                        ((ProgressIndicationAware) context).hideProgressDialog();
                    }
                } catch (IllegalArgumentException e) {
                    return;
                }
            }
            switch (getDynamicPageApiResponse.status) {
                case 0:
                    SectionData sectionData = getDynamicPageApiResponse.apiResponseContent.sectionData;
                    if (sectionData != null && !screenName.equals(SectionManager.MAIN_MENU)) {
                        sectionData.setSections(SectionUtil.preserveMemory(sectionData.getSections()));
                    }
                    if (!silentMode) {
                        ((DynamicScreenAware) context).onDynamicScreenSuccess(screenName, sectionData);
                    }
                    if (sectionData != null && sectionData.getSections() != null &&
                            sectionData.getSections().size() > 0) {
                        sectionManager.storeSectionData(sectionData, getDynamicPageApiResponse.apiResponseContent.cacheDuration);
                    } else {
                        sectionManager.storeSectionData(null, 0);
                    }
                    break;
                default:
                    if (!silentMode) {
                        ((DynamicScreenAware) context).onDynamicScreenFailure(getDynamicPageApiResponse.status,
                                getDynamicPageApiResponse.message);
                    }
                    break;
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (silentMode) return;
            if (((CancelableAware) context).isSuspended()) {
                return;
            }
            try {
                if (inlineProgress) {
                    ((ProgressIndicationAware) context).hideProgressView();
                } else {
                    ((ProgressIndicationAware) context).hideProgressView();
                }
            } catch (IllegalArgumentException e) {
                return;
            }
            ((DynamicScreenAware) context).onDynamicScreenFailure(error);
        }
    }
}
