package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class NowAtBBFragment extends ProductListAwareFragment {
    @Override
    public String getSourceName() {
        return TrackEventkeys.NOW_AT_BB;
    }

    @Override
    @Nullable
    public String getProductListSlug() {
        return null;
    }

    @Override
    public String getProductQueryType() {
        return ProductListType.NOW_AT_BB.get();
    }

    @Override
    public String getTitle() {
        return getString(R.string.nowAtBB);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return NowAtBBFragment.class.getName();
    }
}
