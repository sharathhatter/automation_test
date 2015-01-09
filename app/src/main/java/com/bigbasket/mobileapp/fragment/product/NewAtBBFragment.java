package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class NewAtBBFragment extends ProductListAwareFragment {
    @Override
    public String getSourceName() {
        return TrackEventkeys.NEW_AT_BB;
    }

    @Override
    @Nullable
    public String getProductListSlug() {
        return null;
    }

    @Override
    public String getProductQueryType() {
        return ProductListType.NEW_AT_BB.get();
    }

    @Override
    public String getTitle() {
        return getString(R.string.newLaunches);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return NewAtBBFragment.class.getName();
    }
}
