package com.bigbasket.mobileapp.activity.account.uiv3;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.fragment.account.SpendTrendsBarChartFragment;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;

public class SpendTrendsActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);

        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_spend_trends, null);

        setTabs(base);
        contentFrame.addView(base);
    }

    private void setTabs(View base) {
        final ArrayList<BBTab> bbTabs = new ArrayList<>();

        bbTabs.add(new BBTab<>(getString(R.string.summary), SpendTrendsBarChartFragment.class, null));

        ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);
        FragmentStatePagerAdapter fragmentStatePagerAdapter = new
                TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(), bbTabs);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) base.findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setViewPager(viewPager);
    }
}
