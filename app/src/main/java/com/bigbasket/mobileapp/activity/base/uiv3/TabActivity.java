package com.bigbasket.mobileapp.activity.base.uiv3;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.astuetz.PagerSlidingTabStrip;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;


public abstract class TabActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ArrayList<BBTab> bbTabs = getTabs();

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);

        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_swipe_tab_view, null);
        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setViewPager(viewPager);

        contentLayout.addView(base);
    }

    public abstract ArrayList<BBTab> getTabs();

    private class TabPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<BBTab> bbTabs;

        public TabPagerAdapter(FragmentManager fm, ArrayList<BBTab> bbTabs) {
            super(fm);
            this.bbTabs = bbTabs;
        }

        @Override
        public Fragment getItem(int i) {
            BBTab bbTab = bbTabs.get(i);
            return Fragment.instantiate(getCurrentActivity(), bbTab.getmFragmentClass().getName(),
                    bbTab.getArgs());
        }

        @Override
        public int getCount() {
            return bbTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return bbTabs.get(position).getTabTitle();
        }
    }
}