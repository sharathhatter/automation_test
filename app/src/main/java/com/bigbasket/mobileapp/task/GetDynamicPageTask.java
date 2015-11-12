package com.bigbasket.mobileapp.task;

import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDynamicPageApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.DynamicScreenAware;
import com.bigbasket.mobileapp.managers.SectionManager;
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
        final SectionManager sectionManager = new SectionManager(context.
                getCurrentActivity(), screenName);
        SectionData sectionData = sectionManager.getStoredSectionData();
        if (sectionData != null && sectionData.getSections() != null && sectionData.getSections().size() > 0) {
            context.onDynamicScreenSuccess(screenName, sectionData);
            return;
        }
        if (!context.checkInternetConnection()) {
            context.getHandler().sendOfflineError(finishOnError);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                getApiService(context.getCurrentActivity());
        if (!silentMode) {
            if (inlineProgress) {
                context.showProgressView();
            } else {
                context.showProgressDialog("Please wait...");
            }
        }
        Call<ApiResponse<GetDynamicPageApiResponse>> call;
        switch (screenName) {
            case SectionManager.HOME_PAGE:
                call = bigBasketApiService.getHomePage(Constants.ANDROID,
                        DataUtil.getAppVersion(context.getCurrentActivity()));
                break;
            case SectionManager.MAIN_MENU:
                call = bigBasketApiService.getMainMenu(Constants.ANDROID,
                        DataUtil.getAppVersion(context.getCurrentActivity()));
                break;
            default:
                call = bigBasketApiService.getDynamicPage(Constants.ANDROID,
                        DataUtil.getAppVersion(context.getCurrentActivity()),
                        screenName);
                break;
        }
        call.enqueue(new DynamicPageCallback(context, sectionManager));
    }

    private class DynamicPageCallback extends BBNetworkCallback<ApiResponse<GetDynamicPageApiResponse>> {

        private SectionManager sectionManager;

        private DynamicPageCallback(T ctx, SectionManager sectionManager) {
            super(ctx);
            this.sectionManager = sectionManager;
        }

        @Override
        public void onSuccess(ApiResponse<GetDynamicPageApiResponse> getDynamicPageApiResponse) {
            switch (getDynamicPageApiResponse.status) {
                case 0:
                    SectionData sectionData = getDynamicPageApiResponse.apiResponseContent.sectionData;
                    if (sectionData != null && !screenName.equals(SectionManager.MAIN_MENU)) {
                        sectionData.setSections(SectionUtil.preserveMemory(sectionData.getSections()));
                    }
                    if (!silentMode) {
                        context.onDynamicScreenSuccess(screenName, sectionData);
                    }
                    if (sectionData != null && sectionData.getSections() != null &&
                            sectionData.getSections().size() > 0) {
                        sectionManager.storeSectionData(sectionData, getDynamicPageApiResponse.apiResponseContent.cacheDuration);
                    } else {
                        sectionManager.storeSectionData(null, 0);
                       /* if (screenName.equals("blr-speciality-store")) {
                            Intent intent = new Intent(context.getCurrentActivity(), BBSpecialityShopsActivity.class);
                            intent.putExtra(Constants.CATEGORY, "Cakes");
                            context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }*/
                    }
                    break;
                default:
                    if (!silentMode) {
                        context.onDynamicScreenFailure(getDynamicPageApiResponse.status,
                                getDynamicPageApiResponse.message);
                    }
                    break;
            }
        }

        @Override
        public boolean updateProgress() {
            if (!hideProgress && !silentMode) {
                try {
                    if (inlineProgress) {
                        context.hideProgressView();
                    } else {
                        context.hideProgressDialog();
                    }
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void onFailure(int httpErrorCode, String msg) {
            context.onDynamicScreenFailure(httpErrorCode, msg);
        }

        @Override
        public void onFailure(Throwable t) {
            context.onDynamicScreenFailure(t);
        }
    }
}
