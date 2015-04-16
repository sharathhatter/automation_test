package com.bigbasket.mobileapp.view.uiv3;

import android.support.v4.app.DialogFragment;

import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;


public abstract class AbstractDialogFragment extends DialogFragment {

    public void onResume() {
        super.onResume();
        LocalyticsWrapper.onResume(getScreenTag());
    }

    public abstract String getScreenTag();
}
