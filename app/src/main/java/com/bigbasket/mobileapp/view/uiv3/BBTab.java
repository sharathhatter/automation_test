package com.bigbasket.mobileapp.view.uiv3;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class BBTab<T extends Fragment> {
    private String tabTitle;
    private Class<T> mFragmentClass;
    private Bundle args;

    public BBTab(String tabTitle, Class<T> mFragmentClass) {
        this.tabTitle = tabTitle;
        this.mFragmentClass = mFragmentClass;
    }

    public BBTab(String tabTitle, Class<T> mFragmentClass, Bundle args) {
        this(tabTitle, mFragmentClass);
        this.args = args;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public Class<T> getmFragmentClass() {
        return mFragmentClass;
    }

    public Bundle getArgs() {
        return args;
    }
}
