package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class BundlePackFragment extends ProductListAwareFragment {
    @Override
    public String getSourceName() {
        return TrackEventkeys.BUNDLE_PACK;
    }

    @Override
    @Nullable
    public String getProductListSlug() {
        return null;
    }

    @Override
    public String getProductQueryType() {
        return ProductListType.BUNDLE_PACK.get();
    }

    @Override
    public String getTitle() {
        return getString(R.string.bundlePack);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return BundlePackFragment.class.getName();
    }
}
