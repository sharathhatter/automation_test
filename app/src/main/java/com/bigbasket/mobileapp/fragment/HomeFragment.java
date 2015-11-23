package com.bigbasket.mobileapp.fragment;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.AppCapability;
import com.bigbasket.mobileapp.apiservice.models.response.AppDataResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDynamicPageApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.LoginUserDetails;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateVersionInfoApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.handler.AppDataSyncHandler;
import com.bigbasket.mobileapp.handler.AppUpdateHandler;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.handler.HDFCPayzappHandler;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.receivers.DynamicScreenLoaderCallback;
import com.bigbasket.mobileapp.service.AbstractDynamicPageSyncService;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.LoaderIds;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.SectionCursorHelper;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.AppNotSupportedDialog;
import com.bigbasket.mobileapp.view.uiv2.UpgradeAppDialog;
import com.moengage.widgets.NudgeView;

import java.util.ArrayList;

import retrofit.Call;

public class HomeFragment extends BaseSectionFragment {

    @Nullable
    private RecyclerView mRecyclerView;
    @Nullable
    private ArrayList<Integer> mDynamicTiles;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment_layout, container, false);
        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAppData(savedInstanceState);
        trackEvent(TrackingAware.HOME_PAGE_SHOWN, null);
        setNextScreenNavigationContext(TrackEventkeys.HOME);

    }

    private void homePageGetter(Bundle savedInstanceState) {
        boolean sectionStateRestored = tryRestoreSectionState(savedInstanceState);
        if (sectionStateRestored) {
            renderHomePage();
        } else {
            syncBasket();
            requestHomePage();
        }
        handler = new HomePageHandler<>(this);
    }

    @Override
    protected void onBackResume() {
        super.onBackResume();
        // removed home event from here
        setNextScreenNavigationContext(TrackEventkeys.HOME);
    }

    @Override
    public void onResume() {
        super.onResume();
        // removed home event from here
        AppUpdateHandler.AppUpdateData appUpdateData = AppUpdateHandler.isOutOfDate(getActivity());
        if (appUpdateData != null && !TextUtils.isEmpty(appUpdateData.getAppExpireBy())) {
            showUpgradeAppDialog(appUpdateData.getAppExpireBy(), appUpdateData.getAppUpdateMsg(),
                    appUpdateData.getLatestAppVersion());
        }
    }

    private boolean isVisitorUpdateNeeded() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedVersionNumber = preferences.getString(Constants.VERSION_NAME, null);
        String appVersionName = DataUtil.getAppVersion(getActivity());
        return TextUtils.isEmpty(storedVersionNumber) ||
                (!TextUtils.isEmpty(appVersionName) && !appVersionName.equals(storedVersionNumber));
    }

    private void updateMobileVisitorInfo() {
        // Update app-version number in Mobile Visitor
        if (!checkInternetConnection()) {
            displayHomePageError(getString(R.string.lostInternetConnection), R.drawable.empty_no_internet);
            return;
        }
        AppUpdateHandler.markAsCurrent(getActivity());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        String imei = UIUtil.getIMEI(getActivity());
        Call<ApiResponse<UpdateVersionInfoApiResponseContent>> call =
                bigBasketApiService.updateVersionNumber(imei, preferences.getString(Constants.DEVICE_ID, null),
                        DataUtil.getAppVersion(getActivity()));
        call.enqueue(new BBNetworkCallback<ApiResponse<UpdateVersionInfoApiResponseContent>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<UpdateVersionInfoApiResponseContent> updateVersionInfoApiResponse) {
                switch (updateVersionInfoApiResponse.status) {
                    case 0:
                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                        editor.putString(Constants.VERSION_NAME, DataUtil.getAppVersion(getActivity()));
                        editor.apply();
                        if (updateVersionInfoApiResponse.apiResponseContent.userDetails != null) {
                            UIUtil.updateStoredUserDetails(getActivity(),
                                    updateVersionInfoApiResponse.apiResponseContent.userDetails,
                                    AuthParameters.getInstance(getActivity()).getMemberEmail(),
                                    updateVersionInfoApiResponse.apiResponseContent.mId);
                        }
                        getHomePage();
                        Log.d("HomeFragment", getResources().getString(R.string.versionNoUpdated));
                        break;
                    default:
                        handler.sendEmptyMessage(updateVersionInfoApiResponse.status,
                                updateVersionInfoApiResponse.message, true);
                        break;
                }
            }

            @Override
            public void onFailure(Throwable t) {
                displayHomePageError(getString(R.string.networkError), R.drawable.empty_no_internet);
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressDialog();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });
    }

    private void requestHomePage() {
        if (isVisitorUpdateNeeded()) {
            updateMobileVisitorInfo();
        } else {
            getHomePage();
        }
    }

    private void getHomePage() {
        getLoaderManager().initLoader(LoaderIds.HOME_PAGE_ID, null,
                new DynamicScreenLoaderCallback(getActivity()) {
                    @Override
                    public void onCursorNonEmpty(Cursor data) {
                        hideProgressView();
                        SectionCursorHelper.getSectionDataAsync(data, new SectionCursorHelper.Callback() {
                            @Override
                            public void onParseSuccess(@Nullable GetDynamicPageApiResponse getDynamicPageApiResponse) {
                                handleHomePageResponse(getDynamicPageApiResponse);
                            }

                            @Override
                            public void onParseFailure() {
                                displayHomePageError(getString(R.string.otherError), R.drawable.ic_report_problem_grey600_48dp);
                            }
                        });
                    }

                    @Override
                    public void onCursorLoadingInProgress() {
                        showProgressView();
                    }
                });
    }

    private void handleHomePageResponse(@Nullable GetDynamicPageApiResponse getDynamicPageApiResponse) {
        SectionData sectionData = getDynamicPageApiResponse != null ?
                getDynamicPageApiResponse.sectionData : null;
        if (sectionData != null) {
            onDynamicScreenSuccess(AbstractDynamicPageSyncService.HOME_PAGE, sectionData);
        } else {
            displayHomePageError(getString(R.string.otherError), R.drawable.ic_report_problem_grey600_48dp);
        }
    }

    private void renderHomePage() {
        ViewGroup contentView = getContentView();
        SectionData sectionData = getSectionData();
        if (contentView == null || sectionData == null || sectionData.getSections() == null
                || sectionData.getSections().size() == 0) return;

        // Render sections
        showProgressView();

        contentView.removeAllViews();

        addNudgeView();

        Pair<RecyclerView, ArrayList<Integer>> pair = getSectionRecylerView(contentView);
        mRecyclerView = pair.first;
        if (mRecyclerView != null) {
            contentView.addView(mRecyclerView);
        }
        mDynamicTiles = pair.second;

        // Check if any deep-link needs to be opened
        processPendingDeepLink();
    }

    private void addNudgeView() {
        if (getCurrentActivity() == null || getView() == null) return;
        NudgeView nudgeView = (NudgeView) getView().findViewById(R.id.nudge);
        nudgeView.setMoEHelper(getCurrentActivity().getMoEHelper());
    }

    public void syncDynamicTiles() {
        if (isSuspended() || isDetached()) return;
        if (mDynamicTiles != null && mDynamicTiles.size() > 0 && mRecyclerView != null) {
            for (Integer i : mDynamicTiles) {
                mRecyclerView.getAdapter().notifyItemChanged(i);
            }
        }
    }

    private void processPendingDeepLink() {
        if (getCurrentActivity() == null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String pendingDeepLink = preferences.getString(Constants.DEEP_LINK, null);
        if (!TextUtils.isEmpty(pendingDeepLink)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(Constants.DEEP_LINK);
            editor.apply();
            getCurrentActivity().launchAppDeepLink(pendingDeepLink);
        } else {
            String pendingFragmentCode = preferences.getString(Constants.FRAGMENT_CODE, null);
            if (!TextUtils.isEmpty(pendingFragmentCode)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(Constants.FRAGMENT_CODE);
                editor.apply();
                if (TextUtils.isDigitsOnly(pendingFragmentCode)) {
                    int fragmentCode = Integer.parseInt(pendingFragmentCode);
                    if (fragmentCode == NavigationCodes.GO_TO_BASKET) {
                        getCurrentActivity().launchViewBasketScreen();
                    } else if (getCurrentActivity() instanceof BBActivity) {
                        ((BBActivity) getCurrentActivity()).startFragment(fragmentCode);
                    }
                }
            }
        }
    }

    @Override
    public String getTitle() {
        return "BigBasket";
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        retainSectionState(outState);
        super.onSaveInstanceState(outState);
    }

    private void displayHomePageError(String msg, int errorDrawableId) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        View errorView = getActivity().getLayoutInflater().inflate(R.layout.layout_no_internet, contentView, false);
        TextView txtHeader = (TextView) errorView.findViewById(R.id.txtHeader);
        txtHeader.setVisibility(View.GONE);

        ImageView imgEmptyPage = (ImageView) errorView.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(errorDrawableId);

        TextView txtEmptyMsg1 = (TextView) errorView.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(msg);

        ImageView imgViewRetry = (ImageView) errorView.findViewById(R.id.imgViewRetry);
        imgViewRetry.setImageResource(R.drawable.empty_retry);

        imgViewRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestHomePage();
            }
        });
        contentView.addView(errorView);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return HomeFragment.class.getName();
    }

    private void setAppCapability(AppCapability appCapability) {
        if (appCapability == null) return;
        AuthParameters.getInstance(getCurrentActivity()).setAppCapability(appCapability.isMoEngageEnabled(),
                appCapability.isAnalyticsEnabled(),
                appCapability.isFBLoggerEnabled(),
                appCapability.isMultiCityEnabled(),
                getCurrentActivity());
        AuthParameters.reset();
    }

    private void callGetAppData(String client, String versionName) {
        if (getCurrentActivity() == null) return;
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        Call<ApiResponse<AppDataResponse>> call = bigBasketApiService.getAppData(client, versionName);
        call.enqueue(new BBNetworkCallback<ApiResponse<AppDataResponse>>(this) {
            @Override
            public void onSuccess(ApiResponse<AppDataResponse> callbackAppDataResponse) {
                if (callbackAppDataResponse.status == 0 && getActivity() != null) {
                    String appExpiredBy = callbackAppDataResponse.apiResponseContent.appUpdate.expiryDate;
                    String upgradeMsg = callbackAppDataResponse.apiResponseContent.appUpdate.upgradeMsg;
                    String latestAppVersion = callbackAppDataResponse.apiResponseContent.appUpdate.latestAppVersion;
                    if (!TextUtils.isEmpty(appExpiredBy)) {
                        AppUpdateHandler.markAsOutOfDate(getActivity(), appExpiredBy, upgradeMsg,
                                latestAppVersion);
                    } else {
                        AppUpdateHandler.markAsCurrent(getActivity());
                    }
                    AppCapability appCapability = callbackAppDataResponse.apiResponseContent.capabilities;
                    setAppCapability(appCapability);
                    LoginUserDetails userDetails = callbackAppDataResponse.apiResponseContent.userDetails;
                    if (userDetails != null) {
                        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        UIUtil.updateStoredUserDetails(getCurrentActivity(), userDetails,
                                prefer.getString(Constants.MEMBER_EMAIL_KEY, ""),
                                prefer.getString(Constants.MID_KEY, ""));
                    }
                    HDFCPayzappHandler.setTimeOut(getCurrentActivity(),
                            callbackAppDataResponse.apiResponseContent.hdfcPayzappExpiry);
                    savePopulateSearcher(callbackAppDataResponse.apiResponseContent.topSearches);
                    AppDataSyncHandler.updateLastAppDataCall(getCurrentActivity());
                    CityManager.setCityCacheExpiry(getCurrentActivity(),
                            callbackAppDataResponse.apiResponseContent.cityCacheExpiry);
                }
                // Fail silently
            }

            @Override
            public boolean updateProgress() {
                return true;
            }

            @Override
            public void onFailure(int httpErrorCode, String msg) {
                // Fail silently
            }

            @Override
            public void onFailure(Throwable t) {
                // Fail silently
            }
        });
    }

    private void savePopulateSearcher(ArrayList<String> topSearchList) {
        if (topSearchList == null) return;
        String topSearchCommaSeparatedString = UIUtil.sentenceJoin(topSearchList, ",");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.TOP_SEARCHES, topSearchCommaSeparatedString);
        editor.apply();
    }


    private void showUpgradeAppDialog(String appExpiredBy, String upgradeMsg, String latestAppVersion) {
        if (TextUtils.isEmpty(appExpiredBy)) return;
        int updateValue = AppUpdateHandler.handleUpdateDialog(appExpiredBy.replace("-", "/"), getCurrentActivity());
        switch (updateValue) {
            case Constants.SHOW_APP_UPDATE_POPUP:
                UpgradeAppDialog upgradeAppDialog = UpgradeAppDialog.newInstance(upgradeMsg);
                upgradeAppDialog.show(getFragmentManager(), Constants.APP_UPDATE_DIALOG_FLAG);
                AppUpdateHandler.updateLastPopShownDate(System.currentTimeMillis(), getCurrentActivity());
                break;
            case Constants.SHOW_APP_EXPIRE_POPUP:
                AppNotSupportedDialog appNotSupportedDialog = AppNotSupportedDialog.newInstance(upgradeMsg, latestAppVersion);
                appNotSupportedDialog.show(getFragmentManager(), Constants.APP_EXPIRED_DIALOG_FLAG);
                break;
            default:
                break;
        }
    }

    private void getAppData(Bundle savedInstanceState) {
        if (getCurrentActivity() == null) return;
        if (AppDataSyncHandler.isSyncNeeded(getCurrentActivity())) {
            callGetAppData(Constants.CLIENT_NAME, DataUtil.getAppVersion(getCurrentActivity()));
        }
        homePageGetter(savedInstanceState);
    }

    public void onDynamicScreenSuccess(String screenName, SectionData sectionData) {
        setSectionData(sectionData);
        setScreenName(screenName);
        renderHomePage();
    }

    @Override
    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.HOME_SCREEN;
    }

    public class HomePageHandler<T extends ApiErrorAware & AppOperationAware> extends BigBasketMessageHandler<T> {

        public HomePageHandler(T ctx) {
            super(ctx);
        }

        @Override
        public void sendOfflineError() {
            displayHomePageError(getString(R.string.lostInternetConnection), R.drawable.empty_no_internet);
        }
    }
}
