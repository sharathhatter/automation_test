package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.util.Constants;

public class GenericProductListFragment extends ProductListAwareFragment {
    @Override
    public String getSourceName() {
        return getArguments().getString(Constants.TYPE);
    }

    @Override
    @Nullable
    public String getProductListSlug() {
        return null;
    }

    @Override
    public String getProductQueryType() {
        return getArguments().getString(Constants.TYPE);
    }

    @Override
    public String getTitle() {
        return getString(R.string.nowAtBB);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return GenericProductListFragment.class.getName();
    }
}
