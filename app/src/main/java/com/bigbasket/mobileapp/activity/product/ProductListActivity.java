package com.bigbasket.mobileapp.activity.product;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.astuetz.PagerSlidingTabStrip;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.fragment.product.GenericProductListFragment;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.ProductInfo;
import com.bigbasket.mobileapp.model.product.ProductTabData;
import com.bigbasket.mobileapp.model.product.ProductTabInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.task.uiv3.ProductListTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;
import java.util.HashMap;


public class ProductListActivity extends BBActivity implements ProductListDataAware {

    private ArrayList<NameValuePair> mNameValuePairs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        mNameValuePairs = getIntent().getParcelableArrayListExtra(Constants.PRODUCT_QUERY);
        loadProductTabs();
    }

    private void loadProductTabs() {
        if (mNameValuePairs == null || mNameValuePairs.size() == 0) {
            return;
        }
        HashMap<String, String> paramMap = NameValuePair.toMap(mNameValuePairs);
        new ProductListTask<>(this, paramMap).startTask();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_LISTING_SCREEN;
    }

    public void syncBasket() {
        // Don't remove the IS_BASKET_DIRTY flag, as Fragment also needs to refresh, only update count
        new GetCartCountTask<>(this).startTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void setProductTabData(ProductTabData productTabData) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
        if (productTabData.getProductTabInfos() != null &&
                productTabData.getProductTabInfos().size() > 0) {
            View base = getLayoutInflater().inflate(R.layout.uiv3_swipe_tab_view, contentFrame, false);
            ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);

            ArrayList<BBTab> bbTabs = new ArrayList<>();
            for (ProductTabInfo productTabInfo : productTabData.getProductTabInfos()) {
                ProductInfo productInfo = productTabInfo.getProductInfo();
                if (productInfo != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.PRODUCT_INFO, productInfo);
                    bundle.putString(Constants.BASE_IMG_URL, productTabData.getBaseImgUrl());
                    bundle.putParcelableArrayList(Constants.PRODUCT_QUERY, mNameValuePairs);
                    bundle.putString(Constants.TAB_TYPE, productTabInfo.getTabType());
                    bbTabs.add(new BBTab<>(productTabInfo.getTabName() + " (" + productInfo.getProductCount() + ")",
                            GenericProductListFragment.class, bundle));
                }
            }

            FragmentStatePagerAdapter statePagerAdapter =
                    new TabPagerAdapter(this, getSupportFragmentManager(), bbTabs);
            viewPager.setAdapter(statePagerAdapter);

            PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
            pagerSlidingTabStrip.setViewPager(viewPager);
            contentFrame.addView(base);
            renderHeaderDropDown(productTabData.getHeaderSection());
        } else {
            // TODO: Show empty product view
        }
    }

    private void renderHeaderDropDown(final Section headSection) {
        if (headSection != null && headSection.getSectionItems() != null
                && headSection.getSectionItems().size() > 0) {
            Spinner spinner = new Spinner(this);
            BBArrayAdapter bbArrayAdapter = new BBArrayAdapter<>(this, android.R.layout.simple_spinner_item, headSection.getSectionItems(),
                    faceRobotoRegular, Color.WHITE, getResources().getColor(R.color.uiv3_primary_text_color));
            bbArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(bbArrayAdapter);
            spinner.setSelection(0, false);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != Spinner.INVALID_POSITION) {
                        new OnSectionItemClickListener<>(getCurrentActivity(), headSection, headSection.getSectionItems().get(position), "").onClick(view);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            Toolbar toolbar = getToolbar();
            toolbar.addView(spinner);
        }
    }

    @Override
    public boolean isNextPageLoading() {
        return false;
    }

    @Override
    public void setNextPageLoading(boolean isNextPageLoading) {

    }
}