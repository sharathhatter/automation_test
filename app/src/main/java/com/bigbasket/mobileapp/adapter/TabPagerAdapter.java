package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<BBTab> bbTabs;
    private Context ctx;

    public TabPagerAdapter(Context ctx, FragmentManager fm, ArrayList<BBTab> bbTabs) {
        super(fm);
        this.bbTabs = bbTabs;
        this.ctx = ctx;
    }

    @Override
    public Fragment getItem(int i) {
        BBTab bbTab = bbTabs.get(i);
        return Fragment.instantiate(ctx, bbTab.getmFragmentClass().getName(),
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
