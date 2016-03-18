package com.bigbasket.mobileapp.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.annotation.Nullable;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.crashlytics.android.Crashlytics;

public final class FlatPageHelper {

    private FlatPageHelper() {
    }

    public static void openFlatPage(Activity context, String url, @Nullable String title) {
        url = UIUtil.makeFlatPageUrlAppFriendly(url);
        Intent intent = new Intent(context, BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
        intent.putExtra(Constants.WEBVIEW_URL, url);
        intent.putExtra(Constants.WEBVIEW_TITLE, title);
        try {
            context.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } catch (ActivityNotFoundException ex) {
            Crashlytics.logException(ex);
        }
    }
}
