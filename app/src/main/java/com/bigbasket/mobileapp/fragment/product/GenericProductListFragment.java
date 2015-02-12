package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
        return getArguments().getString(Constants.SLUG);
    }

    @Override
    public String getProductQueryType() {
        return getArguments().getString(Constants.TYPE);
    }

    @Override
    public String getTitle() {
        trackEvent(getArguments().getString(Constants.TRACK_EVENT_NAME), null);
        String title = getArguments().getString(Constants.TITLE);
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.viewProducts);
        }
        return title;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return GenericProductListFragment.class.getName();
    }
}
