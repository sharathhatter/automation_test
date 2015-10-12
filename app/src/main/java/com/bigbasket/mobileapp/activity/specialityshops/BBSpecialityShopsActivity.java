package com.bigbasket.mobileapp.activity.specialityshops;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.specialityshops.StoreListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.StoreListGetApiResponse;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityShopsListData;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityStore;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.HeaderSpinnerView;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BBSpecialityShopsActivity extends BBActivity {

    private TextView mToolbarTextDropDown;
    private String titleFromIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSpecialityShops();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_sstore_list_activity;
    }

    private void getSpecialityShops() {
        titleFromIntent = getIntent().getStringExtra(Constants.TITLE);
        setTitle(titleFromIntent);
//        loadSpecialityShops(mTitlePassedViaIntent);
        loadDummyData(titleFromIntent, "Mahadevapura, Bangalore");
    }

    private void renderStoreList(String baseImgUrl, ArrayList<SpecialityStore> storeList) {
        RecyclerView recyclerViewStoreList = (RecyclerView) findViewById(R.id.store_list);
        StoreListRecyclerAdapter storeListRecyclerAdapter = new StoreListRecyclerAdapter(this, baseImgUrl, storeList);
        recyclerViewStoreList.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStoreList.setAdapter(storeListRecyclerAdapter);
    }

    private void loadDummyData(String catValue, String location) {
        ArrayList<SpecialityStore> list = new ArrayList<SpecialityStore>();
        list.add(new SpecialityStore("img", "Fish Express", "Tubarhalli", "In 90 Minutes", "Opening Time: 07.30AM to 08.00PM"));
        list.add(new SpecialityStore("img", "Meat Mart", "Tubarhalli", "In 60 Minutes", "Opening Time: 07.30AM to 08.00PM"));
        list.add(new SpecialityStore("img", "Fish Express", "Tubarhalli", "In 70 Minutes", "Opening Time: 07.30AM to 08.00PM"));
        list.add(new SpecialityStore("img", "Fish Exp", "Tubarhalli", "In 80 Minutes", "Opening Time: 07.30AM to 08.00PM"));
        list.add(new SpecialityStore("img", "Fish Mart", "Tubarhalli", "In 40 Minutes", "Opening Time: 07.30AM to 08.00PM"));
        list.add(new SpecialityStore("img", "Fish Express", "Tubarhalli", "In 90 Minutes", "Opening Time: 07.30AM to 08.00PM"));
        list.add(new SpecialityStore("img", "Meat Mart", "Tubarhalli", "In 20 Minutes", "Opening Time: 07.30AM to 08.00PM"));
        list.add(new SpecialityStore("img", "Fish Express", "Tubarhalli", "In 10 Minutes", "Opening Time: 07.30AM to 08.00PM"));
        if (list.size() > 0) {
            renderStoreList("", list);
        } else {
            showStoreEmptyMsg(location);
        }
    }

    private void showStoreEmptyMsg(String location) {
        RecyclerView recyclerViewStoreList = (RecyclerView) findViewById(R.id.store_list);
        recyclerViewStoreList.setVisibility(View.GONE);
        LinearLayout layout = (LinearLayout) findViewById(R.id.ssList);
        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_empty_store_list_data, layout, false);

        TextView txtMsg = (TextView) base.findViewById(R.id.textView_empty_text);

        String strMsg = getString(R.string.store_empty) + location + getString(R.string.adding_newStores);
        Spannable spannable = new SpannableString(strMsg);
        spannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), 0, getString(R.string.store_empty).length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannable.setSpan(new CustomTypefaceSpan("", faceRobotoBold), getString(R.string.store_empty).length(), getString(R.string.store_empty).length() + location.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uiv3_status_bar_background)), getString(R.string.store_empty).length(), getString(R.string.store_empty).length() + location.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), getString(R.string.store_empty).length() + location.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtMsg.setText(spannable);

        layout.addView(base);
    }

    private void loadSpecialityShops(String catValue) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getApplicationContext());
        showProgressView();
        bigBasketApiService.getSpecialityShops(catValue, new Callback<ApiResponse<StoreListGetApiResponse>>() {
            @Override
            public void success(ApiResponse<StoreListGetApiResponse> specialityStoreListApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                if (specialityStoreListApiResponse.status == 0) {
                    SpecialityShopsListData specialityShopsListData = specialityStoreListApiResponse.apiResponseContent.specialityShopsListData;
                    Section headerSection = specialityStoreListApiResponse.apiResponseContent.headerSection;
                    renderHeaderDropDown(headerSection, specialityStoreListApiResponse.apiResponseContent.headerSelectedIndex, titleFromIntent);
                    ArrayList<SpecialityStore> storeList = specialityStoreListApiResponse.apiResponseContent.storeList;
                    if (storeList != null && storeList.size() > 0) {
                        renderStoreList(specialityStoreListApiResponse.apiResponseContent.baseImageUrl, storeList);
                    }
                } else {
                    handler.sendEmptyMessage(specialityStoreListApiResponse.status,
                            specialityStoreListApiResponse.message, true);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void renderHeaderDropDown(@Nullable final Section headSection, int mHeaderSelectedIdx,
                                      String screenName) {
        Toolbar toolbar = getToolbar();
        if (mToolbarTextDropDown == null) {
            mToolbarTextDropDown = (TextView) getLayoutInflater().
                    inflate(R.layout.uiv3_product_header_text, toolbar, false);
        }
        new HeaderSpinnerView.HeaderSpinnerViewBuilder<>()
                .withCtx(this)
                .withDefaultSelectedIdx(mHeaderSelectedIdx)
                .withFallbackHeaderTitle(!TextUtils.isEmpty(titleFromIntent) ? titleFromIntent : screenName)
                .withHeadSection(headSection)
                .withImgCloseChildDropdown((ImageView) findViewById(R.id.imgCloseChildDropdown))
                .withLayoutChildToolbarContainer((ViewGroup) findViewById(R.id.layoutChildToolbarContainer))
                .withLayoutListHeader((ViewGroup) findViewById(R.id.layoutListHeader))
                .withListHeaderDropdown((ListView) findViewById(R.id.listHeaderDropdown))
                .withToolbar(getToolbar())
                .withTxtChildDropdownTitle((TextView) findViewById(R.id.txtListDialogTitle))
                .withTxtToolbarDropdown(mToolbarTextDropDown)
                .withTypeface(faceRobotoRegular)
                .build()
                .setView();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SPECIALITYSHOPS_LISTING_PAGE;
    }

    private void logViewSpecialityShopsEvent() {

    }
}
