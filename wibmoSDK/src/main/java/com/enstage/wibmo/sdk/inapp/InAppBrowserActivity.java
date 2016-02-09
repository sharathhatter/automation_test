/*
 * Copyright (C) 2014 enStage Inc. Cupertino, California USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enstage.wibmo.sdk.inapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitResponse;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitResponse;
import com.enstage.wibmo.sdk.R;

/**
 * Created by akshath on 20/10/14.
 */
public class InAppBrowserActivity extends Activity {
    private static final String TAG = "wibmo.sdk.InAppBrowser";

    private W2faInitResponse w2faInitResponse;

    private WPayInitResponse wPayInitResponse;

    private boolean resultSet;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            W2faInitRequest w2faInitRequest = (W2faInitRequest) extras
                    .getSerializable("W2faInitRequest");
            w2faInitResponse = (W2faInitResponse) extras
                    .getSerializable("W2faInitResponse");

            WPayInitRequest wPayInitRequest = (WPayInitRequest) extras
                    .getSerializable("WPayInitRequest");
            wPayInitResponse = (WPayInitResponse) extras
                    .getSerializable("WPayInitResponse");

            String qrMsg;
            if (w2faInitRequest != null && w2faInitResponse!=null) {
                qrMsg = "Wibmo InApp payment";
            } else if (wPayInitRequest != null && wPayInitResponse!=null) {
                qrMsg = "Wibmo InApp payment";
            } else {
                sendAbort();
                return;
            }
        }

        resultSet = false;

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

        LayoutInflater inflator = getLayoutInflater();
        View view = inflator.inflate(R.layout.activity_inapp_browser, null, false);
        view.startAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left));
        setContentView(view);

        WebView webView = (WebView) findViewById(R.id.webView);


        final Activity activity = this;

        webView.setWebViewClient(new WebViewClient() {
            boolean stopCalled = false;

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (WibmoSDKConfig.isTestMode()) {
                    Log.w(TAG, "We have bad certificate.. but this is test mode so okay");
                    Toast.makeText(activity, "Test Mode!! We have bad certificate.. but this is test mode so okay",
                            Toast.LENGTH_SHORT).show();
                    handler.proceed(); // Ignore SSL certificate errors
                } else {
                    Log.w(TAG, "We have bad certificate.. but this is not test!! will abort");
                    Toast.makeText(activity, "We have bad certificate.. will abort!!", Toast.LENGTH_LONG).show();
                    handler.cancel();
                }
            }

            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                Log.e(TAG, "onReceivedError: " + description + "; " + failingUrl);
                Toast.makeText(activity, "Oh no! " + description,
                        Toast.LENGTH_SHORT).show();
            }

            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished: ->" + url + "<-" + stopCalled);
                if (WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "Url " + url,
                            Toast.LENGTH_SHORT).show();
                    */
                }

                view.zoomOut();
            }
        });

        //---
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        //webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);

        //webSettings.setLoadWithOverviewMode(true);
        //webSettings.setUseWideViewPort(true);

        //webView.setPadding(0, 0, 0, 0);
        //webView.setInitialScale(1);

        Log.d(TAG, "Webview: " + webView);
        //--



        final ProgressBar webViewProgressBar = (ProgressBar) findViewById(R.id.web_view_progress_bar);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different
                // scales.
                // The progress meter will automatically disappear when we
                // reach 100%
                Log.i(TAG, "progress: " + progress);
                webViewProgressBar.setProgress(progress);
                if (progress == 100) {
                    webViewProgressBar.setVisibility(View.GONE);
                } else if (webViewProgressBar.getVisibility() == View.INVISIBLE) {
                    webViewProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        class MyJavaScriptInterface {
            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifyAbort() {
                sendAbort();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifyFailure(String resCode, String resDesc) {
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "notifyFailure called",
                            Toast.LENGTH_SHORT).show();
                            */
                }
                sendFailure(resCode, resDesc);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifySuccess(String resCode, String resDesc,
                    String dataPickUpCode, String wTxnId, String msgHash) {
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "notifySuccess called",
                            Toast.LENGTH_SHORT).show();
                            */
                }
                _recordSuccess(resCode, resDesc,
                        dataPickUpCode, wTxnId, msgHash);
                _notifyCompletion();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void recordSuccess(String resCode, String resDesc,
                                      String dataPickUpCode, String wTxnId, String msgHash) {
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "notifySuccess called",
                            Toast.LENGTH_SHORT).show();
                            */
                }
                _recordSuccess(resCode, resDesc,
                        dataPickUpCode, wTxnId, msgHash);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifyCompletion() {
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "notifySuccess called",
                            Toast.LENGTH_SHORT).show();
                            */
                }
                _notifyCompletion();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void toast(String msg) {
                Log.d(TAG, "alert: " + msg);
                showToast(msg);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void alert(String msg) {
                Log.d(TAG, "alert: "+msg);
                showMsg(msg);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void log(String msg) {
                Log.v(TAG, "log: " + msg);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void userCancel() {
                Log.d(TAG, "userCancel");
                onBackPressed();
            }
        }
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "WibmoSDK");
        //webView.clearCache(true);

        if(w2faInitResponse!=null) {
            webView.postUrl(w2faInitResponse.getWebUrl(), "a=b".getBytes());
            Log.i(TAG, "web posting to " + w2faInitResponse.getWebUrl());
        }

        if(wPayInitResponse!=null) {
            webView.postUrl(wPayInitResponse.getWebUrl(), "a=b".getBytes());
            Log.i(TAG, "web posting to " + wPayInitResponse.getWebUrl());
        }

        //webView.requestFocus();
    }



    private void sendAbort() {
        //webView.destroy();

        Intent resultData = new Intent();
        resultData.putExtra("ResCode", "204");
        resultData.putExtra("ResDesc", "user abort");

        if(w2faInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", w2faInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", w2faInitResponse.getTransactionInfo().getMerTxnId());
        } else if(wPayInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", wPayInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", wPayInitResponse.getTransactionInfo().getMerTxnId());
        }

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void sendFailure(String resCode, String resDesc) {
        //webView.destroy();

        Intent resultData = new Intent();
        resultData.putExtra("ResCode", resCode);
        resultData.putExtra("ResDesc", resDesc);

        if(w2faInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", w2faInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", w2faInitResponse.getTransactionInfo().getMerTxnId());
        } else if(wPayInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", wPayInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", wPayInitResponse.getTransactionInfo().getMerTxnId());
        }

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    private void _recordSuccess(String resCode, String resDesc,
            String dataPickUpCode, String wTxnId, String msgHash) {
        //webView.destroy();

        Intent resultData = new Intent();
        resultData.putExtra("ResCode", resCode);
        resultData.putExtra("ResDesc", resDesc);

        resultData.putExtra("DataPickUpCode", dataPickUpCode);
        resultData.putExtra("WibmoTxnId", wTxnId);
        resultData.putExtra("MsgHash", msgHash);

        if(w2faInitResponse!=null) {
            //resultData.putExtra("WibmoTxnId", w2faInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", w2faInitResponse.getTransactionInfo().getMerTxnId());
        } else if(wPayInitResponse!=null) {
            //resultData.putExtra("WibmoTxnId", wPayInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", wPayInitResponse.getTransactionInfo().getMerTxnId());
        }

        setResult(Activity.RESULT_OK, resultData);

        resultSet = true;
    }

    private void _notifyCompletion() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setResult(resultCode, data);
        finish();
    }


    public void processBackAction() {
        if(resultSet) {
            Log.v(TAG, "resultSet was true");
            _notifyCompletion();
        } else {
            Log.v(TAG, "resultSet was false");
            sendAbort();
        }
    }

    @Override
    public void onBackPressed() {
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(
                activity.getString(R.string.confirm_iap_cancel))
                .setPositiveButton(
                        activity.getString(R.string.title_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                processBackAction();
                            }
                        })
                .setNegativeButton(
                        activity.getString(R.string.title_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                if (dialog != null) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, "Error: " + e);
                                    }
                                }
                            }
                        });

        Dialog dialog = builder.create();
        try {
            dialog.show();
        } catch (Throwable e) {
            Log.e(TAG, "Error: " + e, e);

            processBackAction();
        }
    }

    @SuppressLint("NewApi")
    protected void showMsg(String msg) {
        Log.d(TAG, msg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        AlertDialog alert = builder.create();
        alert.setIcon(android.R.drawable.ic_dialog_alert);

        try {
            alert.show();
        } catch(Throwable e) {
            Log.e(TAG, "error: " + e, e);
            showToast(msg);
        }
    }

    private Handler handler = new Handler();
    protected void showToast(final String msg) {
        Log.i(TAG, "Show Toast: " + msg);

        final Activity activity = this;
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
                View view = toast.getView();

                try {
                    toast.show();
                } catch(Throwable e) {
                    Log.e(TAG, "error: " + e, e);
                }
            }
        });
    }
}
