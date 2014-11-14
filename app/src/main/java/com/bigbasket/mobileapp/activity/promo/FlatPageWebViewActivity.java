package com.bigbasket.mobileapp.activity.promo;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.util.Constants;

/**
 * Created by jugal on 7/8/14.
 */
public class FlatPageWebViewActivity extends BaseActivity {
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullfill_info_web_view);
        String fulfillmentInfoPageUrl = getIntent().getStringExtra(Constants.FULFILLED_BY_INFO_PAGE_URL);

        progressBar = (ProgressBar) findViewById(R.id.progressbar_Horizontal);

        Button btnHome = (Button) findViewById(R.id.homeBBbtn);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlatPageWebViewActivity.this.finish();
            }
        });
        WebView wevViewFulfillmentPage = (WebView) findViewById(R.id.wevViewFulfillmentPage);
        wevViewFulfillmentPage.getSettings().setJavaScriptEnabled(true);
        wevViewFulfillmentPage.getSettings().setLoadWithOverviewMode(true); // fit to webview
        wevViewFulfillmentPage.getSettings().setUseWideViewPort(true); // supporting tags
        if (fulfillmentInfoPageUrl != null) {
            wevViewFulfillmentPage.loadUrl(fulfillmentInfoPageUrl);
            wevViewFulfillmentPage.setWebViewClient(new MyWebViewClient());
        }

        wevViewFulfillmentPage.setWebChromeClient((new WebChromeClient() {

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

//    private Map<String, String> getAuthParams(){
//        final AuthParameters authParameters = AuthParameters.getInstance(this);
//         return new HashMap<String, String>() {{
//            put("Cookie", "_bb_vid=\""+authParameters.getVisitorId());
//            put("\";BBAUTHTOKEN=\"", authParameters.getBbAuthToken()+"\"");
//            put("User-Agent", Constants.USER_AGENT_PREFIX +authParameters.getOsVersion());
//        }};
//    }


    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        //show the web page in webview but not in web browser
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }
}