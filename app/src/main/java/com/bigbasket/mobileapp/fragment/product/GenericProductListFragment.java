package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

public class GenericProductListFragment extends ProductListAwareFragment {

    @Override
    public String getNavigationCtx() {
        return getArguments().getString(TrackEventkeys.NAVIGATION_CTX);
    }

    @Nullable
    @Override
    public ArrayList<NameValuePair> getInputForApi() {
        return getArguments().getParcelableArrayList(Constants.PRODUCT_QUERY);
    }

    @Override
    public String getTitle() {
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
