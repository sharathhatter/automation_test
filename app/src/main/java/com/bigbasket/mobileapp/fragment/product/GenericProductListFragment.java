package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

public class GenericProductListFragment extends ProductListAwareFragment {
    @Override
    public String getSourceName() {
        // TODO : Jugal please check this, it won't be passed anymore
        return getArguments().getString(Constants.TYPE);
    }

    @Nullable
    @Override
    public ArrayList<NameValuePair> getInputForApi() {
        return getArguments().getParcelableArrayList(Constants.PRODUCT_QUERY);
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
