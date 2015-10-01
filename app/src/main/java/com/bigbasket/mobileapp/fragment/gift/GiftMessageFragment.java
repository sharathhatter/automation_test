package com.bigbasket.mobileapp.fragment.gift;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;

/**
 * Created by manu on 28/9/15.
 */
public class GiftMessageFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_giftmessage_fragment, container, false);
    }

    @Override
    public String getTitle() {
        return getString(R.string.giftOptions);
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return null;
    }

    @Override
    public String getScreenTag() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return GiftMessageFragment.class.getName();
    }
}
