package com.bigbasket.mobileapp.activity.product;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.fragment.product.DiscountFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.discount.DiscountDataModel;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionUtil;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;


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
        Call<ApiResponse<DiscountDataModel>> call = bigBasketApiService.getDiscount();
        call.enqueue(new BBNetworkCallback<ApiResponse<DiscountDataModel>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<DiscountDataModel> discountDataModelApiResponse) {
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


    private void renderDiscountFragments(final SectionData categorySectionData, final SectionData binSectionData) {

        if (categorySectionData != null && categorySectionData.getSections() != null
                && categorySectionData.getSections().size() > 0 &&
                binSectionData != null && binSectionData.getSections() != null
                && binSectionData.getSections().size() > 0) {

            ArrayList<BBTab> bbTabs = new ArrayList<>();
            createTabFragment(categorySectionData, bbTabs);
            createTabFragment(binSectionData, bbTabs);

            ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
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
                    if (position == 0 && categorySectionData.getScreenName() != null) {
                        eventAttribs.put(Constants.TAB_NAME, categorySectionData.getScreenName());
                    } else if (binSectionData.getScreenName() != null) {
                        eventAttribs.put(Constants.TAB_NAME, binSectionData.getScreenName());
                    }
                    eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                    trackEvent(TrackingAware.DISCOUNT_TAB_CHANGED, eventAttribs);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });


            TabLayout pageTitleStrip = (TabLayout) findViewById(R.id.slidingTabs);
            pageTitleStrip.setupWithViewPager(viewPager);

        }
        trackEvent(TrackingAware.DISCOUNT_SHOWN, null, null, null, false, true);
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

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_swipe_tabview_with_drawer;
    }
}
