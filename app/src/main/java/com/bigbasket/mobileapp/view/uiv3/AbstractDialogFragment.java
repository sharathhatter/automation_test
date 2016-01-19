package com.bigbasket.mobileapp.view.uiv3;

import android.support.v7.app.AppCompatDialogFragment;

import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;


public abstract class AbstractDialogFragment extends AppCompatDialogFragment {

    public void onResume() {
        super.onResume();
        LocalyticsWrapper.tagScreen(getScreenTag());
    }

    protected abstract String getScreenTag();
}
