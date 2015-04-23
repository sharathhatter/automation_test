package com.bigbasket.mobileapp.activity.promo;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.BBWebView;


public class FlatPageWebViewActivity extends BackButtonActivity {
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullfill_info_web_view);
        String webViewUrl = getIntent().getStringExtra(Constants.WEBVIEW_URL);
        String webViewTitle = getIntent().getStringExtra(Constants.WEBVIEW_TITLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!TextUtils.isEmpty(webViewTitle)) {
            getSupportActionBar().setTitle(webViewTitle);
        }
        progressBar = (ProgressBar) findViewById(R.id.progressbar_Horizontal);

        BBWebView bbWebView = (BBWebView) findViewById(R.id.webViewFulfillmentPage);
        bbWebView.getSettings().setJavaScriptEnabled(true);
        bbWebView.getSettings().setDomStorageEnabled(true);
        if (webViewUrl != null) {
            if (!webViewUrl.contains("source=app")) {
                if (webViewUrl.contains("?")) {
                    webViewUrl += "&source=app";
                } else {
                    webViewUrl += "?source=app";
                }
            }
            bbWebView.loadUrl(webViewUrl); // To get responsive template
            bbWebView.setWebViewClient(new FulFillmentWebViewClient());
        }

        bbWebView.setWebChromeClient((new WebChromeClient() {

            // this will be called on page loading progress

            @Override

            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                //loadingTitle.setProgress(newProgress);
                // hide the progress bar if the loading is complete
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        }));

    }


    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.FLAT_PAGE_SCREEN;
    }

    private class FulFillmentWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}