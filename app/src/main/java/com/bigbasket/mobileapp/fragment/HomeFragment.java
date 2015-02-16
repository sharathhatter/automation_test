package com.bigbasket.mobileapp.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.AnalyticsEngine;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.AppDataResponse;
import com.bigbasket.mobileapp.apiservice.models.response.LoginUserDetails;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateVersionInfoApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.DynamicScreenAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.task.GetDynamicPageTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.AppNotSupportedDialog;
import com.bigbasket.mobileapp.view.uiv2.UpgradeAppDialog;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeFragment extends BaseSectionFragment implements DynamicScreenAware {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAppData(savedInstanceState);
    }

    private void homePageGetter(Bundle savedInstanceState){
        boolean sectionStateRestored = tryRestoreSectionState(savedInstanceState);
        if (sectionStateRestored) {
            renderHomePage();
        } else {
            new GetCartCountTask<>(getCurrentActivity(), true).startTask();
            requestHomePage();
        }
        handler = new HomePageHandler<>(this);
    }

    @Override
    public void onBackResume() {
        super.onBackResume();
        new GetCartCountTask<>(getCurrentActivity(), true).startTask();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) getActivity()).removePharmaPrescriptionId();
    }

    private boolean isVisitorUpdateNeeded() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedVersionNumber = preferences.getString(Constants.VERSION_NAME, null);
        String appVersionName = getAppVersion();
        return TextUtils.isEmpty(storedVersionNumber) ||
                (!TextUtils.isEmpty(appVersionName) && !appVersionName.equals(storedVersionNumber));
    }

    private void updateMobileVisitorInfo() {
        // Update app-version number in Mobile Visitor
        if (!checkInternetConnection()) {
            displayHomePageError(getString(R.string.deviceOfflineSmallTxt), R.drawable.ic_signal_wifi_off_grey600_48dp);
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.updateVersionNumber(preferences.getString(Constants.DEVICE_ID, null),
                getAppVersion(), new Callback<ApiResponse<UpdateVersionInfoApiResponseContent>>() {
                    @Override
                    public void success(ApiResponse<UpdateVersionInfoApiResponseContent> updateVersionInfoApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }

                        switch (updateVersionInfoApiResponse.status) {
                            case 0:
                                SharedPreferences.Editor editor =
                                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                editor.putString(Constants.VERSION_NAME, getAppVersion());
                                editor.commit();
                                if (updateVersionInfoApiResponse.apiResponseContent.userDetails != null) {
                                    UIUtil.updateStoredUserDetails(getActivity(),
                                            updateVersionInfoApiResponse.apiResponseContent.userDetails,
                                            AuthParameters.getInstance(getActivity()).getMemberEmail(),
                                            updateVersionInfoApiResponse.apiResponseContent.mId);
                                    AuthParameters.updateInstance(getActivity());
                                    if (getCurrentActivity() != null &&
                                            !AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
                                        getCurrentActivity().updateKonotor();
                                    }
                                }
                                getHomePage();
                                Log.d("HomeFragment", getResources().getString(R.string.versionNoUpdated));
                                break;
                            default:
                                Intent result = new Intent();
                                result.putExtra(Constants.FORCE_REGISTER_DEVICE, true);
                                getActivity().setResult(Constants.FORCE_REGISTER_CODE, result);
                                if (getCurrentActivity() == null) return;
                                getCurrentActivity().onLogoutRequested();
                                getActivity().finish();
                                break;
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (error.getKind() == RetrofitError.Kind.NETWORK) {
                            displayHomePageError(getString(R.string.networkError), R.drawable.ic_signal_wifi_off_grey600_48dp);
                        } else {
                            handler.handleRetrofitError(error);
                        }
                    }
                });
    }

    private String getAppVersion() {
        String appVersionName;
        try {
            appVersionName = getActivity().getPackageManager().
                    getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionName = null;
        }
        return appVersionName;
    }

    private void requestHomePage() {
        if (isVisitorUpdateNeeded()) {
            updateMobileVisitorInfo();
        } else {
            getHomePage();
        }
    }

    private void getHomePage() {
        if (getActivity() == null) return;
        new GetDynamicPageTask<>(this, "home-page", true, true).startTask();
    }

    private void handleHomePageRetrofitError(RetrofitError error) {
        switch (error.getKind()) {
            case NETWORK:
                displayHomePageError(getString(R.string.networkError), R.drawable.ic_signal_wifi_off_grey600_48dp);
                break;
            case HTTP:
                displayHomePageError(getString(R.string.communicationError), R.drawable.ic_signal_wifi_off_grey600_48dp);
                break;
            default:
                displayHomePageError(getString(R.string.otherError), R.drawable.ic_report_problem_grey600_48dp);
                break;
        }
    }

    private void renderHomePage() {
        LinearLayout contentView = getContentView();
        if (contentView == null || mSectionData == null || mSectionData.getSections() == null
                || mSectionData.getSections().size() == 0) return;

        // Render sections
        showProgressView();

        ScrollView contentScrollView = new ScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentScrollView.setLayoutParams(layoutParams);

        View sectionView = getSectionView();
        if (sectionView != null) {
            contentScrollView.addView(sectionView);
        }

        contentView.removeAllViews();
        contentView.addView(contentScrollView);
    }

    @Override
    public String getTitle() {
        return "BigBasket";
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        retainSectionState(outState);
        super.onSaveInstanceState(outState);
    }

    private void displayHomePageError(String msg, int errorDrawableId) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();
        View base = getActivity().getLayoutInflater().inflate(R.layout.uiv3_inline_error_page, contentView, false);
        TextView txtInlineErrMsg = (TextView) base.findViewById(R.id.txtInlineErrorMsg);
        ImageView imgInlineError = (ImageView) base.findViewById(R.id.imgInlineError);
        Button btnRetry = (Button) base.findViewById(R.id.btnRetry);

        txtInlineErrMsg.setTypeface(faceRobotoRegular);
        btnRetry.setTypeface(faceRobotoRegular);

        txtInlineErrMsg.setText(msg);
        imgInlineError.setImageResource(errorDrawableId);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestHomePage();
            }
        });
        contentView.addView(base);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return Constants.HOME;
    }

    private void setAnalyticalData(AnalyticsEngine analyticsEngine) {
        if (analyticsEngine == null) return;
        AuthParameters.getInstance(getCurrentActivity()).setAnyLyticsEnabled(analyticsEngine.isMoEngageEnabled(),
                analyticsEngine.isAnalyticsEnabled(), analyticsEngine.isKonotorEnabled(), getCurrentActivity());
        AuthParameters.updateInstance(getCurrentActivity());
    }

    private void callGetAppData(String client, String versionName, final Bundle savedInstanceState) {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) handler.sendOfflineError();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.getAppData(client, versionName,
                new Callback<ApiResponse<AppDataResponse>>() {
                    @Override
                    public void success(ApiResponse<AppDataResponse> callbackAppDataResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (callbackAppDataResponse.status == 0) {
                            UIUtil.updateLastAppDataCall(getCurrentActivity());
                            String appExpiredBy = callbackAppDataResponse.apiResponseContent.appUpdate.expiryDate;
                            String upgradeMsg = callbackAppDataResponse.apiResponseContent.appUpdate.upgradeMsg;
                            showUpgradeAppDialog(appExpiredBy, upgradeMsg);
                            AnalyticsEngine analyticsEngine = callbackAppDataResponse.apiResponseContent.capabilities;
                            setAnalyticalData(analyticsEngine);
                            LoginUserDetails userDetails = callbackAppDataResponse.apiResponseContent.userDetails;
                            if (userDetails != null) {
                                SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                UIUtil.updateStoredUserDetails(getCurrentActivity(), userDetails,
                                        prefer.getString(Constants.MEMBER_EMAIL_KEY, ""),
                                        prefer.getString(Constants.MID_KEY, ""));//TODO: check with sid
                            }
                            homePageGetter(savedInstanceState);
                        } else {
                            handler.sendEmptyMessage(callbackAppDataResponse.status);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        trackEvent(TrackingAware.MY_ACCOUNT_CURRENT_PIN_FAILED, null);
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }

                });
    }


    private void showUpgradeAppDialog(String appExpiredBy, String upgradeMsg) {
        if (appExpiredBy == null) return;
        int updateValue = UIUtil.handleUpdateDialog(appExpiredBy.replace("-", "/"), getCurrentActivity());
        switch (updateValue) {
            case Constants.SHOW_APP_UPDATE_POPUP:
                UpgradeAppDialog upgradeAppDialog = UpgradeAppDialog.newInstance(upgradeMsg);
                upgradeAppDialog.show(getFragmentManager(), Constants.APP_UPDATE_DIALOG_FLAG);
                UIUtil.updateLastPopShownDate(System.currentTimeMillis(), getCurrentActivity());
                break;
            case Constants.SHOW_APP_EXPIRE_POPUP:
                AppNotSupportedDialog appNotSupportedDialog = AppNotSupportedDialog.newInstance();
                appNotSupportedDialog.show(getFragmentManager(), Constants.APP_EXPIRED_DIALOG_FLAG);
                break;
            default:
                break;
        }
    }

    private void getAppData(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long lastAppDataCallTime = preferences.getLong(Constants.LAST_APP_DATA_CALL_TIME, 0);
        if (lastAppDataCallTime == 0 || UIUtil.isMoreThanXHour(lastAppDataCallTime, Constants.SIX_HOUR)) {
            try {
                callGetAppData(Constants.CLIENT_NAME, DataUtil.getAppVersionName(getCurrentActivity()),
                        savedInstanceState);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            homePageGetter(savedInstanceState);
        }
    }


    @Override
    public void onDynamicScreenSuccess(String screenName, SectionData sectionData) {
        mSectionData = sectionData;
        renderHomePage();
    }

    @Override
    public void onDynamicScreenFailure(RetrofitError error) {
        handleHomePageRetrofitError(error);
    }

    @Override
    public void onDynamicScreenFailure(int error, String msg) {
        displayHomePageError(getString(R.string.otherError), R.drawable.ic_report_problem_grey600_48dp);
    }

    public class HomePageHandler<T> extends BigBasketMessageHandler<T> {

        public HomePageHandler(T ctx) {
            super(ctx);
        }

        @Override
        public void sendOfflineError() {
            displayHomePageError(getString(R.string.deviceOfflineSmallTxt), R.drawable.ic_signal_wifi_off_grey600_48dp);
        }
    }

    @Override
    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.HOME_SCREEN;
    }
}
