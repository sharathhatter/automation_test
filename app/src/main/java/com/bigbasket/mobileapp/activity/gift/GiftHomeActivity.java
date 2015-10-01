package com.bigbasket.mobileapp.activity.gift;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.gift.GiftHomeActivityPageAdapter;
import com.bigbasket.mobileapp.interfaces.GiftAddMessageButtonClickListener;
import com.bigbasket.mobileapp.util.UIUtil;

/**
 * Created by manu on 25/9/15.
 */
public class GiftHomeActivity extends BackButtonActivity implements GiftAddMessageButtonClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /******rendering the gift stage view***********/
        LinearLayout mGiftStageLinearLayout=(LinearLayout) findViewById(R.id.mGiftStageLinearLayout);
        renderCheckOutProgressView(mGiftStageLinearLayout);
        /*********rendering the view pager tabs for the gift list and add message tabs***/
        renderViewPagerTabs();

    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_gift_homeactivity_layout;
    }

    private void renderCheckOutProgressView(LinearLayout layout) {
        String[] array_txtValues = new String[]{"Address", "Gift", "Slots", "Order"};
        Integer[] array_compPos = new Integer[]{0};
        int selectedPos = 1;
        View giftView = UIUtil.getCheckoutProgressView(this, null, array_txtValues, array_compPos, selectedPos);
        if (giftView != null) layout.addView(giftView, 0);
    }

    private void renderViewPagerTabs(){
        final ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setAdapter(new GiftHomeActivityPageAdapter(getSupportFragmentManager()));

        TabLayout tabLayout=(TabLayout) findViewById(R.id.slidingTabs);
        tabLayout.addTab(tabLayout.newTab().setText("1. Select Quantity"));
        tabLayout.addTab(tabLayout.newTab().setText("2. Add Message"));

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public void addGiftMessage() {
        // add message button clicked
    }
}
