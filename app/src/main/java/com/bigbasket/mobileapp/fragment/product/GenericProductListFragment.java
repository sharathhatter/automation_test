package com.bigbasket.mobileapp.fragment.product;

import android.support.annotation.NonNull;

import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;

public class GenericProductListFragment extends ProductListAwareFragment {

    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return GenericProductListFragment.class.getName();
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "GenericProductListFragment";
    }
}
