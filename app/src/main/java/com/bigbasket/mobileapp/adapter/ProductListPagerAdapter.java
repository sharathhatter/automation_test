package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ProductListPagerAdapter extends TabPagerAdapter {

    private ArrayList<WeakReference<Fragment>> fragments;

    public ProductListPagerAdapter(Context ctx, FragmentManager fm, ArrayList<BBTab> bbTabs) {
        super(ctx, fm, bbTabs);
        this.fragments = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        if (i < fragments.size()) {
            fragment = fragments.get(i).get();
        }
        if (fragment != null) {
            return fragment;
        }
        fragment = super.getItem(i);
        if (i >= fragments.size()) {
            fragments.add(i, new WeakReference<>(fragment));
        } else {
            fragments.set(i, new WeakReference<>(fragment));
        }
        return fragment;
    }
}
