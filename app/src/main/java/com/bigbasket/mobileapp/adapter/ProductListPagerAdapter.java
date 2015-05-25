package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ProductListPagerAdapter extends TabPagerAdapter {

    private SparseArray<WeakReference<Fragment>> registeredfragments;

    public ProductListPagerAdapter(Context ctx, FragmentManager fm, ArrayList<BBTab> bbTabs) {
        super(ctx, fm, bbTabs);
        this.registeredfragments = new SparseArray<>();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredfragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredfragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Nullable
    public Fragment getRegisteredFragment(int position) {
        WeakReference<Fragment> fragmentWeakReference = registeredfragments.get(position);
        if (fragmentWeakReference != null) {
            fragmentWeakReference.get();
        }
        return null;
    }
}
