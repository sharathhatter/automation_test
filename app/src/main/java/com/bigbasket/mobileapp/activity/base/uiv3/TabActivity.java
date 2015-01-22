package com.bigbasket.mobileapp.activity.base.uiv3;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.astuetz.PagerSlidingTabStrip;
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

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);

        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_swipe_tab_view, contentLayout, false);

        setTabContent(base);
        contentLayout.addView(base);
    }

    public void setTabContent(View base) {
        final ArrayList<BBTab> bbTabs = getTabs();

        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setViewPager(viewPager);
    }

    public abstract ArrayList<BBTab> getTabs();

}