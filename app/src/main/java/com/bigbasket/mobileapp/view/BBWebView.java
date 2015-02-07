package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

public class BBWebView extends WebView {
    public BBWebView(Context context) {
        super(context);
    }

    public BBWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BBWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            super.loadDataWithBaseURL(null, data, mimeType, encoding, null);
        } else {
            super.loadData(data, mimeType, encoding);
        }
    }
}
