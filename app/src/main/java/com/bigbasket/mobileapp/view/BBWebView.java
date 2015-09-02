package com.bigbasket.mobileapp.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bigbasket.mobileapp.util.NavigationCodes;

import java.lang.ref.WeakReference;

public class BBWebView extends WebView {
    public BBWebView(Context context) {
        super(context);
        setWebViewClient(new BBWebViewClient(context));
    }

    public BBWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWebViewClient(new BBWebViewClient(context));
    }

    public BBWebView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWebViewClient(new BBWebViewClient(context));
    }

    public static class BBWebViewClient extends WebViewClient {

        private WeakReference<Context> activityWeakReference;

        public BBWebViewClient(Context context) {
            this.activityWeakReference = new WeakReference<>(context);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && url.startsWith("bigbasket://")
                    && activityWeakReference != null && activityWeakReference.get() != null) {
                try {
                    ((Activity) activityWeakReference.get())
                            .startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                                    NavigationCodes.GO_TO_HOME);
                    return true;
                } catch (ActivityNotFoundException e) {
                    // Do nothing
                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
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
