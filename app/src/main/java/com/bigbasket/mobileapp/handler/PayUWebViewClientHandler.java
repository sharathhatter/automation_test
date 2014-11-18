package com.bigbasket.mobileapp.handler;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.util.Constants;


public class PayUWebViewClientHandler extends WebViewClient {

    private BaseActivity baseActivity;
    private ProgressDialog progressBar;
    private boolean isInProgress;
    private String pid, succesCaptureUrl, failureCaptureUrl;

    public PayUWebViewClientHandler(BaseActivity bigBasketActivity, String pid,
                                    String succesCaptureUrl,
                                    String failureCaptureUrl) {
        this.baseActivity = bigBasketActivity;
        this.pid = pid;
        this.succesCaptureUrl = succesCaptureUrl;
        this.failureCaptureUrl = failureCaptureUrl;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (!isInProgress) {
            progressBar = ProgressDialog.show(baseActivity, "", "Please wait");
            isInProgress = true;
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        progressBar.dismiss();
        if (url != null) {
            if (url.contains(succesCaptureUrl)) {
                PayuResponse.createInstance(baseActivity,
                        url, true, pid);
                PayuResponse payuResponse = PayuResponse.getInstance(baseActivity);
                if (payuResponse == null) {
                    baseActivity.showAlertDialog("Server Error");
                    return;
                }
                baseActivity.setResult(Constants.PAYU_SUCCESS);
                baseActivity.finish();
            } else if (url.contains(failureCaptureUrl)) {
                PayuResponse.createInstance(baseActivity,
                        url, false, pid);
                PayuResponse payuResponse = PayuResponse.getInstance(baseActivity);
                if (payuResponse == null) {
                    baseActivity.showAlertDialog("Server Error");
                    return;
                }
                baseActivity.setResult(Constants.PAYU_FAILED);
                baseActivity.finish();
            }
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }


}
