package com.bigbasket.mobileapp.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.BBWebView;

import java.util.HashMap;

public class FlatPageFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fullfill_info_web_view, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args == null || getView() == null) return;
        String webViewUrl = args.getString(Constants.WEBVIEW_URL);

        final ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progressbar_Horizontal);

        BBWebView bbWebView = (BBWebView) getView().findViewById(R.id.webViewFulfillmentPage);
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
        bbWebView.setWebViewClient(new BBWebView.BBWebViewClient(getActivity()) {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setTitle(view.getTitle());
            }
        });

        HashMap<String, String> map = new HashMap<>();
        if (!TextUtils.isEmpty(webViewUrl))
            map.put(TrackEventkeys.URL, webViewUrl);
        trackEvent(TrackingAware.FLAT_PAGE_SHOWN, map);
    }

    @Override
    public String getTitle() {
        return getArguments() != null ? getArguments().getString(Constants.WEBVIEW_TITLE) :
                null;
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.layoutContentView) : null;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.FLAT_PAGE_SCREEN;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return FlatPageFragment.class.getName();
    }
}
