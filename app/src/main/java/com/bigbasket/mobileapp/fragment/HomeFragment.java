package com.bigbasket.mobileapp.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateVersionInfoApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.model.SectionManager;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.Iterator;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeFragment extends BaseSectionFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean sectionStateRestored = tryRestoreSectionState(savedInstanceState);
        if (sectionStateRestored) {
            renderHomePage();
        } else {
            new GetCartCountTask<>(getCurrentActivity(), true).startTask();
            requestHomePage();
        }
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
        final SectionManager sectionManager = new SectionManager(getActivity(), SectionManager.HOME_PAGE_SECTION);
        mSectionData = sectionManager.getStoredSectionData();
        if (mSectionData != null) {
            renderHomePage();
            return;
        }
        if (!checkInternetConnection()) {
            displayHomePageError(getString(R.string.deviceOfflineSmallTxt), R.drawable.ic_signal_wifi_off_grey600_48dp);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.loadHomePage(new Callback<ApiResponse<SectionData>>() {
            @Override
            public void success(ApiResponse<SectionData> homePageApiResponse, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                switch (homePageApiResponse.status) {
                    case 0:
                        mSectionData = homePageApiResponse.apiResponseContent;
                        sectionManager.storeSectionData(mSectionData);
                        renderHomePage();
                        break;
                    default:
                        displayHomePageError(getString(R.string.otherError), R.drawable.ic_report_problem_grey600_48dp);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handleHomePageRetrofitError(error);
            }
        });
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

    private void parseRendererColors() {
        if (mSectionData == null || mSectionData.getRenderersMap() == null) return;
        int defaultTextColor = getResources().getColor(R.color.uiv3_list_secondary_text_color);
        for (Renderer renderer : mSectionData.getRenderersMap().values()) {
            renderer.setNativeBkgColor(UIUtil.parseAsNativeColor(renderer.getBackgroundColor(), Color.WHITE));
            renderer.setNativeTextColor(UIUtil.parseAsNativeColor(renderer.getTextColor(), defaultTextColor));
        }
    }

    private void renderHomePage() {
        LinearLayout contentView = getContentView();
        parseRendererColors();
        if (contentView == null || mSectionData == null || mSectionData.getSections() == null
                || mSectionData.getSections().size() == 0) return;
        // Filter sections
        Set<String> supportedSectionTypes = Section.getSupportedSectionTypes();
        for (Iterator<Section> iterator = mSectionData.getSections().iterator(); iterator.hasNext(); ) {
            Section section = iterator.next();
            if (!supportedSectionTypes.contains(section.getSectionType())) {
                iterator.remove();
            }
        }

        // Render sections
        showProgressView();

        ScrollView contentScrollView = new ScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentScrollView.setLayoutParams(layoutParams);

        LinearLayout homePageLayout = new LinearLayout(getActivity());
        homePageLayout.setOrientation(LinearLayout.VERTICAL);

        displaySections(homePageLayout);
        contentScrollView.addView(homePageLayout);

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
}
