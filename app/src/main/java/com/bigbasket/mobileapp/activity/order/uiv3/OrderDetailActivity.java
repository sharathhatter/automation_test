package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.TabActivity;
import com.bigbasket.mobileapp.activity.payment.PayNowActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.fragment.order.OrderInvoiceItemsListFragment;
import com.bigbasket.mobileapp.fragment.order.OrderInvoiceSummaryFragment;
import com.bigbasket.mobileapp.fragment.order.OrderModificationFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OrderDetailActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.order_details);
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.order_detail_invoice_download, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download_invoice:
                OrderInvoice orderInvoice = getIntent().getParcelableExtra(Constants.ORDER_REVIEW_SUMMARY);
                if (orderInvoice == null || UIUtil.isEmpty(orderInvoice.getInvoiceDownloadUrl()))
                    return false;

                downloadInvoice(orderInvoice.getInvoiceDownloadUrl());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadInvoice(String invoiceDownloadUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(invoiceDownloadUrl);
            intent.setData(uri);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Crashlytics.logException(ex);
            showToast("No application found to open the report.");
        }
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
    public void setTabContent() {
        final ArrayList<BBTab> bbTabs = getTabs();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        final TabLayout pagerSlidingTabStrip = (TabLayout) findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setupWithViewPager(viewPager);
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
        eventAttribs.put(Constants.TAB_NAME, TrackEventkeys.ITEM_TAB);
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
        trackEvent(TrackingAware.ORDER_ITEMS_TAB_CHANGED, eventAttribs);
    }


    @Override
    public String getScreenTag() {
        return TrackEventkeys.ORDER_DETAILS_SUMMARY_SCREEN;
    }
}