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
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.discount.DiscountDataModel;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionUtil;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class DiscountActivity extends BBActivity {


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NC_DISCOUNT_SCREEN);
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
                    SectionData categoryDiscount = discountDataModelApiResponse.apiResponseContent.categoryDiscount;
                    if (categoryDiscount != null) {
                        categoryDiscount.setSections(SectionUtil.preserveMemory(categoryDiscount.getSections()));
                    }
                    SectionData percentageDiscount = discountDataModelApiResponse.apiResponseContent.percentageDiscount;
                    if (percentageDiscount != null) {
                        percentageDiscount.setSections(SectionUtil.preserveMemory(percentageDiscount.getSections()));
                    }
                    renderDiscountFragments(categoryDiscount, percentageDiscount);
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


    private void renderDiscountFragments(final SectionData categorySectionData, final SectionData binSectionData) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        if (contentFrame == null) return;
        contentFrame.removeAllViews();

        if (categorySectionData != null && categorySectionData.getSections() != null
                && categorySectionData.getSections().size() > 0 &&
                binSectionData != null && binSectionData.getSections() != null
                && binSectionData.getSections().size() > 0) {

            View view = getLayoutInflater().inflate(R.layout.uiv3_swipe_tab_view, contentFrame, false);

            ArrayList<BBTab> bbTabs = new ArrayList<>();
            createTabFragment(categorySectionData, bbTabs);
            createTabFragment(binSectionData, bbTabs);

            ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
            TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(),
                    bbTabs);
            viewPager.setAdapter(tabPagerAdapter);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    HashMap<String, String> eventAttribs = new HashMap<>();
                    if(position==0 && categorySectionData.getScreenName()!=null){
                        eventAttribs.put(Constants.TYPE, categorySectionData.getScreenName());
                    }else if(binSectionData.getScreenName()!=null){
                        eventAttribs.put(Constants.TYPE, binSectionData.getScreenName());
                    }
                    trackEvent(TrackingAware.DISCOUNT_TAB_CHANGED, eventAttribs);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });


            SmartTabLayout pageTitleStrip = (SmartTabLayout) view.findViewById(R.id.slidingTabs);
            pageTitleStrip.setDistributeEvenly(true);
            pageTitleStrip.setViewPager(viewPager);

            contentFrame.addView(view);
        } else {
            SectionData availableSectionData = null;
            if (categorySectionData != null && categorySectionData.getSections() != null
                    && categorySectionData.getSections().size() > 0) {
                availableSectionData = categorySectionData;
            } else if (binSectionData != null && binSectionData.getSections() != null
                    && binSectionData.getSections().size() > 0) {
                availableSectionData = binSectionData;
            }

            if (availableSectionData != null) {
                DiscountFragment discountFragment = new DiscountFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.SECTIONS, availableSectionData);
                discountFragment.setArguments(bundle);
                onChangeFragment(discountFragment);
            }
        }

        trackEvent(TrackingAware.DISCOUNT_SHOWN, null);
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
