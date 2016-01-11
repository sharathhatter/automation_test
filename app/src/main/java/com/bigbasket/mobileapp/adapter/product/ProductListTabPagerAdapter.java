package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.adapter.TabPagerAdapterWithFragmentRegistration;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.ads.SponsoredAds;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;

/**
 * Created by muniraju on 21/12/15.
 */
public class ProductListTabPagerAdapter extends TabPagerAdapterWithFragmentRegistration {

    private SponsoredAds sponsoredProducts;

    public ProductListTabPagerAdapter(Context ctx, FragmentManager fm, ArrayList<BBTab> bbTabs) {
        super(ctx, fm, bbTabs);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        if (fragment instanceof ProductListAwareFragment) {
            ((ProductListAwareFragment) fragment).setSponsoredSectionData(sponsoredProducts);
        }
        return fragment;
    }

    public SponsoredAds getSponsoredProducts() {
        return sponsoredProducts;
    }

    public void setSponsoredProducts(SponsoredAds sponsoredProducts) {
        this.sponsoredProducts = sponsoredProducts;
    }
}
