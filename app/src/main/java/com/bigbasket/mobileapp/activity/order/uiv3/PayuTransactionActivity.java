package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPaymentParamsApiResponseContent;
import com.bigbasket.mobileapp.handler.PayUWebViewClientHandler;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PayuTransactionActivity extends BackButtonActivity {

    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        mWebView = (WebView) findViewById(R.id.webView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("");
        startPaymentGateway();
    }

    private void startPaymentGateway() {
        final String potentialOrderId = getIntent().getStringExtra(Constants.POTENTIAL_ORDER_ID);
        String amount = getIntent().getStringExtra(Constants.FINAL_PAY);
        if (!TextUtils.isEmpty(potentialOrderId)) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.getPaymentParams(potentialOrderId, amount, new Callback<ApiResponse<GetPaymentParamsApiResponseContent>>() {
                @Override
                public void success(ApiResponse<GetPaymentParamsApiResponseContent> getPaymentParamsApiResponse, Response response) {
                    if (isSuspended()) return;
                    try {
                        hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    switch (getPaymentParamsApiResponse.status) {
                        case 0:
                            openPayuGateway(potentialOrderId, getPaymentParamsApiResponse.apiResponseContent.payuGatewayUrl,
                                    getPaymentParamsApiResponse.apiResponseContent.payuPostParamsJson.toString(),
                                    getPaymentParamsApiResponse.apiResponseContent.successCaptureUrl,
                                    getPaymentParamsApiResponse.apiResponseContent.failureCaptureUrl);
                            break;
                        default:
                            showAlertDialog(null, getString(R.string.server_error),
                                    Constants.PAYU_CANCELLED);
                            break;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (isSuspended()) return;
                    try {
                        hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    handler.handleRetrofitError(error, Constants.PAYU_CANCELLED);
                }
            });
        }
    }

    private void openPayuGateway(String potentialOrderId,
                                 String payuGatewayUrl,
                                 String payuPostParamStr,
                                 String successCaptureUrl,
                                 String failureCaptureUrl) {
        // Set _bb_vid and BBAUTHTOKEN cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        AuthParameters authParameters = AuthParameters.getInstance(this);
        cookieManager.setCookie(MobileApiUrl.DOMAIN, "_bb_vid=" + authParameters.getVisitorId());
        cookieManager.setCookie(MobileApiUrl.DOMAIN, "BBAUTHTOKEN=" + authParameters.getBbAuthToken());

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        mWebView.setWebViewClient(new PayUWebViewClientHandler(
                this, potentialOrderId,
                successCaptureUrl, failureCaptureUrl));
        mWebView.loadData(getPayuWebViewContent(payuGatewayUrl, payuPostParamStr), "text/html", null);
    }

    private Map<String, String> getPayuInputParamMap(String payuPostParamStrJson) {
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return new Gson().fromJson(payuPostParamStrJson, type);
    }

    private String getPayuWebViewContent(String payuGatewayUrl, String payuPostParamJson) {
        String htmlContent = "<html><head>" +
                "<script type=\"text/javascript\">window.onload = function() { var form = document.getElementsByTagName('form')[0]; form.submit();}</script>" +
                "</head><body><h4>Redirecting to Payment Gateway, Please wait..</h4><form action=\"" + payuGatewayUrl + "\" method=\"POST\">";
        Map<String, String> payuPostParamMap = getPayuInputParamMap(payuPostParamJson);
        for (Map.Entry<String, String> entry : payuPostParamMap.entrySet()) {
            htmlContent += "<input type=\"hidden\" name=\"" + entry.getKey()
                    + "\" value=\"" + entry.getValue() + "\"/>";
        }
        htmlContent += "</form></body></html>";
        return htmlContent;
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.PAYU_CANCELLED:
                    PayuResponse.clearTxnDetail(this);
                    setResult(Constants.PAYU_ABORTED);
                    finish();
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showAlertDialog(null, getString(R.string.abortPayu), Constants.PAYU_CANCELLED);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            showAlertDialog(null, getString(R.string.abortPayu), Constants.PAYU_CANCELLED);
        }
    }
}