package com.bigbasket.mobileapp.activity.base.uiv3;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;


public abstract class TabActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTabs();
    }

    public void setTabs() {
        setTabContent();
    }

    public void setTabContent() {
        final ArrayList<BBTab> bbTabs = getTabs();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        TabLayout pagerSlidingTabStrip = (TabLayout) findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setupWithViewPager(viewPager);
    }

    public abstract ArrayList<BBTab> getTabs();

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_swipe_tab_view;
    }
}