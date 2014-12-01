package com.bigbasket.mobileapp.fragment.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.CheckoutQCActivity;
import com.bigbasket.mobileapp.handler.MessageHandler;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.UIUtil;

import org.apache.http.impl.client.BasicCookieStore;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseFragment extends AbstractFragment implements HandlerAware,
        CartInfoAware, BasketOperationAware, COReserveQuantityCheckAware, ProgressIndicationAware,
        ConnectivityAware, TrackingAware {

    protected Handler handler;
    private ProgressDialog progressDialog;
    protected COReserveQuantity coReserveQuantity;

    private BasketOperationResponse basketOperationResponse;
    protected CartSummary cartInfo = new CartSummary();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = new MessageHandler((BaseActivity) activity, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null) {
            getView().setClickable(true);
        }
        setTitle();
    }

    @Override
    public void onBackResume() {
        super.onBackResume();
        setTitle();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(true);
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseActivity) {
            ((BaseActivity) activity).triggerActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startAsyncActivity(String url, HashMap<String, String> params, boolean post,
                                   boolean inlineProgress, HashMap<Object, String> additionalCtx) {
        startAsyncActivity(url, params, post, inlineProgress, null, additionalCtx);
    }

    public void startAsyncActivity(String url, HashMap<String, String> params, boolean post,
                                   boolean inlineProgress, String loadingMsg,
                                   HashMap<Object, String> additionalCtx) {
        assert getActivity() != null : "No activity attached to the fragment";
        BaseActivity baseActivity = (BaseActivity) getActivity();
        if (DataUtil.isInternetAvailable(baseActivity)) {
            if (isSuspended()) {
                return;
            }
            if (inlineProgress) {
                showProgressView();
            } else {
                showProgressDialog(loadingMsg);
            }
            AuthParameters authParameters = AuthParameters.getInstance(baseActivity);
            HttpRequestData httpRequestData = new HttpRequestData(url, params, post,
                    authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                    authParameters.getOsVersion(), new BasicCookieStore(), additionalCtx);
            new HttpAsyncActivity(inlineProgress).execute(httpRequestData);
        } else {
            baseActivity.getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
        }
    }

    private class HttpAsyncActivity extends AsyncTask<HttpRequestData, Integer, HttpOperationResult> {

        private boolean inlineProgress;

        private HttpAsyncActivity(boolean inlineProgress) {
            this.inlineProgress = inlineProgress;
        }

        protected HttpOperationResult doInBackground(HttpRequestData... httpRequestDatas) {
            if (isCancelled()) {
                return null;
            }
            HttpRequestData httpRequestData = httpRequestDatas[0];
            HttpOperationResult httpOperationResult;
            httpOperationResult = httpRequestData.isPost() ? DataUtil.doHttpPost(httpRequestData)
                    : DataUtil.doHttpGet(httpRequestData);
            return httpOperationResult;
        }

        protected void onPostExecute(HttpOperationResult httpOperationResult) {
            if (isSuspended()) {
                return;
            }
            if (inlineProgress) {
                hideProgressView();
            } else {
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
            }
            Log.d("OnPostExecute", "");
            if (httpOperationResult != null) {
                onAsyncTaskComplete(httpOperationResult);
            } else {
                onHttpError();
            }
        }
    }

    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {

    }

    public void onHttpError() {

    }

    public void showProgressView() {
        if (getActivity() == null) return;
        LinearLayout view = getContentView();
        if (view == null) return;
        view.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View loadingView = inflater.inflate(R.layout.uiv3_loading_layout, null);
        view.addView(loadingView);
    }

    public void hideProgressView() {
        if (getActivity() == null) return;
        LinearLayout view = getContentView();
        if (view == null) return;
        view.removeAllViews();
    }

    public void showProgressDialog(String msg) {
        if (TextUtils.isEmpty(msg)) {
            msg = getResources().getString(R.string.please_wait);
        }
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public boolean checkInternetConnection() {
        return getActivity() != null && ((BaseActivity) getActivity()).checkInternetConnection();
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    public void changeFragment(AbstractFragment newFragment) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().onChangeFragment(newFragment);
    }

    private void changeTitle(String title) {
        if (getCurrentActivity() != null) {
            getCurrentActivity().onChangeTitle(title);
        }
    }

    public void setTitle() {
        String title = getTitle();
        changeTitle(title);
    }

    public void setTitle(String title) {
        changeTitle(title);
    }

    public abstract String getTitle();

    public void showErrorMsg(String msg) {
        // Change it later on
        ((BaseActivity) getActivity()).showAlertDialog(msg);
    }

    public void showInlineErrorMsg(String msg) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_inline_error_msg, null);
        TextView txtErrMsg = (TextView) base.findViewById(R.id.txtErrorMsg);
        txtErrMsg.setText(msg);
        contentView.removeAllViews();
        contentView.addView(base);
    }

    @Nullable
    public abstract LinearLayout getContentView();

    public void finishFragment() {

    }

    @Nullable
    public BaseActivity getCurrentActivity() {
        return getActivity() != null ? (BaseActivity) getActivity() : null;
    }

    public Spannable asRupeeSpannable(double amt) {
        return UIUtil.asRupeeSpannable(amt, faceRupee);
    }

    public Spannable asRupeeSpannable(String amtTxt) {
        return UIUtil.asRupeeSpannable(amtTxt, faceRupee);
    }

    public BasketOperationResponse getBasketOperationResponse() {
        return basketOperationResponse;
    }

    @Override
    public void setBasketOperationResponse(BasketOperationResponse basketOperationResponse) {
        this.basketOperationResponse = basketOperationResponse;
    }

    @Override
    public void updateUIAfterBasketOperationFailed(BasketOperation basketOperation, TextView basketCountTextView,
                                                   ImageView imgDecQty, ImageView imgIncQty, Button btnAddToBasket,
                                                   EditText editTextQty, Product product, String qty) {
        if (basketOperationResponse.getErrorType().equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(getActivity(), "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    ImageView imgDecQty, ImageView imgIncQty, Button btnAddToBasket,
                                                    EditText editTextQty, Product product, String qty) {

        int productQtyInBasket = Integer.parseInt(basketOperationResponse.getTotalQuantity());
        int totalProductsInBasket = Integer.parseInt(basketOperationResponse.getNoOfItems());

        if (basketOperation == BasketOperation.ADD) {
            if (productQtyInBasket == 1) {
                Toast.makeText(getActivity(), "Product added to basket.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Product quantity increased in the basket.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (basketOperation == BasketOperation.EMPTY || productQtyInBasket == 0) {
                Toast.makeText(getActivity(), "Product removed from basket.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Product quantity reduced from basket.", Toast.LENGTH_SHORT).show();
            }
        }

        if (productQtyInBasket == 0) {
            if (imgDecQty != null) {
                imgDecQty.setVisibility(View.GONE);
            }
            if (imgIncQty != null) {
                imgIncQty.setVisibility(View.GONE);
            }
            if (btnAddToBasket != null) {
                btnAddToBasket.setVisibility(View.VISIBLE);
            }
            if (editTextQty != null) {
                editTextQty.setText("1");
                editTextQty.setVisibility(View.VISIBLE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setVisibility(View.GONE);
            }
        } else {
            if (imgDecQty != null) {
                imgDecQty.setVisibility(View.VISIBLE);
            }
            if (imgIncQty != null) {
                imgIncQty.setVisibility(View.VISIBLE);
            }
            if (btnAddToBasket != null) {
                btnAddToBasket.setVisibility(View.GONE);
            }
            if (editTextQty != null) {
                editTextQty.setVisibility(View.GONE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setText(productQtyInBasket + " in basket");
                basketCountTextView.setVisibility(View.VISIBLE);
            }
        }

        if (product != null) {
            product.setNoOfItemsInCart(productQtyInBasket);
        }

        SharedPreferences.Editor editor = getActivity().getSharedPreferences("myCustomSharedPrefs", Activity.MODE_PRIVATE).edit();
        editor.putString(Constants.GET_CART, String.valueOf(totalProductsInBasket));
        editor.commit();
        cartInfo.setNoOfItems(totalProductsInBasket);
    }

    @Override
    public void setCartInfo(CartSummary cartInfo) {
        this.cartInfo = cartInfo;
    }

    @Override
    public CartSummary getCartInfo() {
        return cartInfo;
    }

    @Override
    public void updateUIForCartInfo() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(Constants.GET_CART, "" + cartInfo.getNoOfItems());
        editor.commit();
    }

    public void showAlertDialog(Context context, String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName,
                                final Object passedValue, String positiveBtnText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(!TextUtils.isEmpty(title) ? title : "BigBasket");
        builder.setMessage(msg);
        if (dialogButton != null && nxtDialogButton != null) {
            if (dialogButton.equals(DialogButton.YES) || dialogButton.equals(DialogButton.OK)) {
                if (TextUtils.isEmpty(positiveBtnText)) {
                    int textId = dialogButton.equals(DialogButton.YES) ? R.string.yesTxt : R.string.ok;
                    positiveBtnText = getString(textId);
                }
                builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onPositiveButtonClicked(dialogInterface, id, sourceName, passedValue);
                    }
                });
            }
            if (nxtDialogButton.equals(DialogButton.NO))
                builder.setNegativeButton(R.string.noTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onNegativeButtonClicked(dialogInterface, id, sourceName);
                    }
                });
        }
        AlertDialog alertDialog = builder.create();
        //if (isSuspended())
        //    return;
        alertDialog.show();
    }

    public void showAlertDialogForGoToHome(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("BigBasket");
        alertDialogBuilder.setMessage(msg).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                goToHome();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        //if (isSuspended())
        //    return;
        alertDialog.show();
    }


    public void showAlertDialogFinish(Context context, String title,
                                      String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title == null ? "BigBasket" : title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishFragment();
            }
        });
        AlertDialog alertDialog = builder.create();
        //if (isSuspended())
        //    return;
        alertDialog.show();
    }

    public void goToHome() {
        //setResult(Constants.GO_TO_HOME);
        //getCurrentActivity().finish();
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, @Nullable String sourceName, Object valuePassed) {
    }

    protected void onNegativeButtonClicked(DialogInterface dialogInterface, int id, String sourceName) {

    }


    @Override
    public COReserveQuantity getCOReserveQuantity() {
        return coReserveQuantity;
    }

    @Override
    public void setCOReserveQuantity(COReserveQuantity coReserveQuantity) {
        this.coReserveQuantity = coReserveQuantity;
    }

    @Override
    public void onCOReserveQuantityCheck() {
        if (coReserveQuantity.isStatus()) {
            Intent intent = new Intent(getActivity(), CheckoutQCActivity.class);
            intent.putExtra(Constants.QC_LEN, coReserveQuantity.getQc_len());
            startActivityForResult(intent, Constants.GO_TO_HOME);
        }
    }

    public String getFloatAmount(float amount) {
        int amountInt = (int) amount;
        if (amountInt == amount)
            return String.valueOf(amountInt);
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);
        return (nf.format(amount).equals("0.00") || nf.format(amount).equals("0.0")) ? "0" : nf.format(amount);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().trackEvent(eventName, eventAttribs);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source, String sourceValue) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().trackEvent(eventName, eventAttribs, source, sourceValue);
    }
}