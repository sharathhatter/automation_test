package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;


public class BrowseByOffersFragment extends ProductListAwareFragment {

    @Override
    @Nullable
    public String getProductListSlug() {
        return Constants.ALL_OFFERS;
    }

    @Override
    public String getProductQueryType() {
        return ProductListType.CATEGORY.get();
    }

    @Override
    public String getTitle() {
        return getString(R.string.discount);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return BrowseByOffersFragment.class.getName();
    }

    @Override
    public void updateData() {
        super.updateData();
        trackEvent(TrackingAware.BROWSE_DISCOUNTS, null);
    }

    @Override
    public String getSourceName() {
        return TrackEventkeys.BROWSE_BY_OFFERS;
    }
}