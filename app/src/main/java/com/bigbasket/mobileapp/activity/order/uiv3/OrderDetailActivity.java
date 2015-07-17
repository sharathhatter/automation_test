package com.bigbasket.mobileapp.activity.order.uiv3;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.TabActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.fragment.order.OrderInvoiceItemsListFragment;
import com.bigbasket.mobileapp.fragment.order.OrderInvoiceSummaryFragment;
import com.bigbasket.mobileapp.fragment.order.OrderModificationFragment;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OrderDetailActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Order Details");
    }

    @Override
    public ArrayList<BBTab> getTabs() {
        OrderInvoice orderInvoice = getIntent().getParcelableExtra(Constants.ORDER_REVIEW_SUMMARY);

        ArrayList<BBTab> bbTabs = new ArrayList<>();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ACTION_TAB_TAG, orderInvoice);
        bbTabs.add(new BBTab<>(getString(R.string.summary), OrderInvoiceSummaryFragment.class, bundle));
        bbTabs.add(new BBTab<>(getString(R.string.items), OrderInvoiceItemsListFragment.class, bundle));

        if (orderInvoice != null && orderInvoice.getOrderModifications() != null && orderInvoice.getOrderModifications().size() > 0) {
            bbTabs.add(new BBTab<>(getString(R.string.orderModification), OrderModificationFragment.class, bundle));
        }
        return bbTabs;
    }

    @Override
    public void setTabContent(View base) {
        final ArrayList<BBTab> bbTabs = getTabs();

        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        final SmartTabLayout pagerSlidingTabStrip = (SmartTabLayout) base.findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setDistributeEvenly(true);
        pagerSlidingTabStrip.setViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                OrderInvoice orderInvoice = getIntent().getParcelableExtra(Constants.ORDER_REVIEW_SUMMARY);
                if (position == 1) logOrderItemTabClicked(orderInvoice);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void logOrderItemTabClicked(OrderInvoice orderInvoice) {
        if (orderInvoice == null) return;
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ORDER_ID, orderInvoice.getOrderNumber());
        trackEvent(TrackingAware.ORDER_ITEMS_TAB_CLICKED, eventAttribs);
    }


    @Override
    public String getScreenTag() {
        return TrackEventkeys.ORDER_DETAILS_SUMMARY_SCREEN;
    }
}