package com.bigbasket.mobileapp.activity.gift;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.fragment.gift.GiftItemListFragment;
import com.bigbasket.mobileapp.fragment.gift.GiftMessageFragment;
import com.bigbasket.mobileapp.interfaces.gift.GiftItemAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;


public class GiftHomeActivity extends BackButtonActivity implements GiftItemAware {

    private Gift gift;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gift = getIntent().getParcelableExtra(Constants.GIFTS);
        /******rendering the gift stage view***********/
        renderCheckOutProgressView();
        /*********rendering the view pager tabs for the gift list and add message tabs***/
        renderViewPagerTabs();

    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_gift_homeactivity_layout;
    }

    private void renderCheckOutProgressView() {
        ViewGroup layoutGiftProgressView = (ViewGroup) findViewById(R.id.layoutGiftProgressView);
        String[] array_txtValues = new String[]{"Address", "Gift", "Slots", "Order"};
        Integer[] array_compPos = new Integer[]{0};
        int selectedPos = 1;
        View giftView = UIUtil.getCheckoutProgressView(this, null, array_txtValues, array_compPos, selectedPos);
        if (giftView != null) layoutGiftProgressView.addView(giftView);
    }

    private void renderViewPagerTabs() {
        final ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.slidingTabs);

        ArrayList<BBTab> bbTabs = new ArrayList<>();
        bbTabs.add(new BBTab<>(getString(R.string.selectQtySnum),
                GiftItemListFragment.class));
        bbTabs.add(new BBTab<>(getString(R.string.addMsgSnum),
                GiftMessageFragment.class));

        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(this, getSupportFragmentManager(),
                bbTabs);
        pager.setAdapter(tabPagerAdapter);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(pager);

    }

    @Override
    public Gift getGifts() {
        return gift;
    }
}
