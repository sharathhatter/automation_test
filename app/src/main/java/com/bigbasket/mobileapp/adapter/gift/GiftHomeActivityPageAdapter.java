package com.bigbasket.mobileapp.adapter.gift;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bigbasket.mobileapp.fragment.gift.GiftItemListFragment;
import com.bigbasket.mobileapp.fragment.gift.GiftMessageFragment;

/**
 * Created by manu on 25/9/15.
 */
public class GiftHomeActivityPageAdapter extends FragmentPagerAdapter {

    Bundle giftBundle;
    public GiftHomeActivityPageAdapter(FragmentManager fm) {
        super(fm);
//        this.giftBundle=giftBundle;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0: GiftItemListFragment giftItemListFragment=new GiftItemListFragment();
                    giftItemListFragment.setArguments(giftBundle);
                    return giftItemListFragment;
            case 1: return new GiftMessageFragment();

        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

}
