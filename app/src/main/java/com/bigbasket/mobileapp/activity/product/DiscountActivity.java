package com.bigbasket.mobileapp.activity.product;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.fragment.product.DiscountFragment;
import com.bigbasket.mobileapp.model.discount.DiscountDataModel;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class DiscountActivity extends BBActivity {


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setTitle(getString(R.string.discounts));
        getDiscountData();
    }


    private void getDiscountData() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getApplicationContext());
        showProgressView();
        bigBasketApiService.getDiscount(new Callback<ApiResponse<DiscountDataModel>>() {
            @Override
            public void success(ApiResponse<DiscountDataModel> discountDataModelApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                if (discountDataModelApiResponse.status == 0) {
                    renderDiscountFragments(discountDataModelApiResponse.apiResponseContent.categoryDiscount,
                            discountDataModelApiResponse.apiResponseContent.percentageDiscount);
                } else {
                    handler.sendEmptyMessage(discountDataModelApiResponse.status,
                            discountDataModelApiResponse.message, true);
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


    private void renderDiscountFragments(SectionData categorySectionData, SectionData binSectionData) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        if (contentFrame == null) return;
        contentFrame.removeAllViews();

        View view = getLayoutInflater().inflate(R.layout.uiv3_swipe_tab_view, contentFrame, false);

        ArrayList<BBTab> bbTabs = new ArrayList<>();
        createTabFragment(categorySectionData, bbTabs);
        createTabFragment(binSectionData, bbTabs);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(),
                bbTabs);
        viewPager.setAdapter(tabPagerAdapter);


        SmartTabLayout pageTitleStrip = (SmartTabLayout) view.findViewById(R.id.slidingTabs);
        pageTitleStrip.setDistributeEvenly(true);
        pageTitleStrip.setViewPager(viewPager);

        contentFrame.addView(view);
    }

    private ArrayList<BBTab> createTabFragment(SectionData categorySectionData, ArrayList<BBTab> bbTabs) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SECTIONS, categorySectionData);
        bbTabs.add(new BBTab<>(categorySectionData.getScreenName(), DiscountFragment.class, bundle));
        return bbTabs;
    }


    @Override
    public String getScreenTag() {
        return TrackEventkeys.DISCOUNT_SCREEN;
    }

}
