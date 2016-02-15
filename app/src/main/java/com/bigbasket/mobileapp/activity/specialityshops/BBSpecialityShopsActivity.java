package com.bigbasket.mobileapp.activity.specialityshops;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.specialityshops.StoreListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.LaunchStoreListAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityShopsListData;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityStore;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.HeaderSpinnerView;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;

public class BBSpecialityShopsActivity extends BackButtonActivity implements LaunchStoreListAware {

    private TextView mToolbarTextDropDown;
    private String category;
    private RecyclerView recyclerViewStoreList;
    private HeaderSpinnerView mHeaderSpinnerView;
    private TextView emptyMsgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerViewStoreList = (RecyclerView) findViewById(R.id.store_list);
        emptyMsgView = (TextView) findViewById(R.id.textView_empty_text);
        getSpecialityShops();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_speciality_store_list_activity;
    }

    @Override
    protected void postLogout(boolean success) {
        super.postLogout(success);
        goToHome();
    }

    @Override
    protected void changeCity(City city) {
        super.changeCity(city);
        goToHome();
    }

    private void getSpecialityShops() {
        if (getIntent() != null) {
            category = getIntent().getStringExtra(Constants.CATEGORY);
            setTitle(getString(R.string.speciality_shop_title));
            loadSpecialityShops(category);
        }
    }

    private void renderStoreList(String baseImgUrl, ArrayList<SpecialityStore> storeList) {
        recyclerViewStoreList.setVisibility(View.VISIBLE);
        emptyMsgView.setVisibility(View.GONE);
        UIUtil.configureRecyclerView(recyclerViewStoreList, this, 1, 1);
        StoreListRecyclerAdapter<BBSpecialityShopsActivity> storeListRecyclerAdapter =
                new StoreListRecyclerAdapter<>(BBSpecialityShopsActivity.this, baseImgUrl, storeList);
        recyclerViewStoreList.setAdapter(storeListRecyclerAdapter);
        setCurrentScreenName(TrackingAware.SPECIALITYSHOPS + storeList.get(0).getStoreName());
        logViewSpecialityShopsEvent(category);
    }

    private void showStoreEmptyMsg(String location) {
        recyclerViewStoreList.setVisibility(View.GONE);
        emptyMsgView.setVisibility(View.VISIBLE);

        String emptyMsg = getString(R.string.store_empty) + category + getString(R.string.available_in) + " \n";
        if (TextUtils.isEmpty(location)) {
            emptyMsg = getString(R.string.store_empty) + category + getString(R.string.available_in);
            location = getString(R.string.your_loc);
        }
        String strMsg = emptyMsg + location + getString(R.string.adding_newStores);
        Spannable spannable = new SpannableString(strMsg);
        spannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), 0, emptyMsg.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        if (!location.equalsIgnoreCase(getString(R.string.your_loc))) {
            spannable.setSpan(new CustomTypefaceSpan("", faceRobotoBold), emptyMsg.length(), emptyMsg.length() + location.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.uiv3_status_bar_background)), emptyMsg.length(), emptyMsg.length() + location.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else
            spannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), emptyMsg.length(), emptyMsg.length() + location.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), emptyMsg.length() + location.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        emptyMsgView.setText(spannable);
    }

    private void loadSpecialityShops(final String catVal) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getApplicationContext());
        showProgressView();
        Call<ApiResponse<SpecialityShopsListData>> call = bigBasketApiService.getSpecialityShops(getPreviousScreenName(), catVal);
        call.enqueue(new BBNetworkCallback<ApiResponse<SpecialityShopsListData>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<SpecialityShopsListData> specialityStoreListApiResponse) {
                if (specialityStoreListApiResponse.status == 0) {
                    Section headerSection = specialityStoreListApiResponse.apiResponseContent.getHeaderSection();
                    if (headerSection != null && headerSection.getSectionItems().size() > 0) {
                        renderHeaderDropDown(headerSection, specialityStoreListApiResponse.apiResponseContent.getHeaderSelectedIndex(),
                                getString(R.string.speciality_shop_title));
                    }
                    ArrayList<SpecialityStore> storeList = specialityStoreListApiResponse.apiResponseContent.getStoreList();
                    if (storeList != null && storeList.size() > 0) {
                        renderStoreList(specialityStoreListApiResponse.apiResponseContent.getBaseImgUrl(), storeList);
                    } else {
                        final ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(BBSpecialityShopsActivity.this).getAddressSummaries();
                        if (addressSummaries != null && addressSummaries.size() > 0) {
                            showStoreEmptyMsg(addressSummaries.get(0).getArea() + "," + addressSummaries.get(0).getCityName());
                            renderHeaderDropDown(null, 0, getString(R.string.speciality_shop_title));
                        } else showStoreEmptyMsg(null);
                    }
                } else {
                    handler.sendEmptyMessage(specialityStoreListApiResponse.status,
                            specialityStoreListApiResponse.message, true);
                }
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressView();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mHeaderSpinnerView != null && mHeaderSpinnerView.isShown()) {
            mHeaderSpinnerView.hide();
        } else {
            super.onBackPressed();
        }
    }

    private void renderHeaderDropDown(@Nullable final Section headSection, int mHeaderSelectedIdx,
                                      String screenName) {
        Toolbar toolbar = getToolbar();
        if (mToolbarTextDropDown == null) {
            mToolbarTextDropDown = (TextView) getLayoutInflater().
                    inflate(R.layout.uiv3_product_header_text, toolbar, false);
        }
        if (mHeaderSpinnerView == null) {
            mHeaderSpinnerView = new HeaderSpinnerView.HeaderSpinnerViewBuilder<>()
                    .withCtx(this)
                    .withImgCloseChildDropdown((ImageView) findViewById(R.id.imgCloseChildDropdown))
                    .withLayoutChildToolbarContainer((ViewGroup) findViewById(R.id.layoutChildToolbarContainer))
                    .withLayoutListHeader((ViewGroup) findViewById(R.id.layoutListHeader))
                    .withListHeaderDropdown((ListView) findViewById(R.id.listHeaderDropdown))
                    .withToolbar(getToolbar())
                    .withTxtChildDropdownTitle((TextView) findViewById(R.id.txtListDialogTitle))
                    .withTxtToolbarDropdown(mToolbarTextDropDown)
                    .withTypeface(faceRobotoRegular)
                    .build();
        }
        mHeaderSpinnerView.setDefaultSelectedIdx(mHeaderSelectedIdx);
        mHeaderSpinnerView.setFallbackHeaderTitle(
                !TextUtils.isEmpty(screenName) ? screenName : getString(R.string.app_name));
        mHeaderSpinnerView.setHeadSection(headSection);
        mHeaderSpinnerView.setView();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SPECIALITYSHOPS_LISTING_PAGE;
    }

    private void logViewSpecialityShopsEvent(String categoryName) {
        if (categoryName == null) return;
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.CATEGORY, categoryName);
        trackEvent(TrackingAware.SPECIALITYSHOPS_LIST_SHOWN, map);
        trackEventAppsFlyer(TrackingAware.SPECIALITYSHOPS_LIST_SHOWN);
    }

    @Override
    public void launchStoreList(String destinationSlug) {
        category = destinationSlug;
        loadSpecialityShops(category);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
        getSpecialityShops();
    }
}
