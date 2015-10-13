package com.bigbasket.mobileapp.fragment.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.interfaces.AnalyticsNavigationContextAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.task.uiv3.CreateShoppingListTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseFragment extends AbstractFragment implements HandlerAware,
        CartInfoAware, BasketOperationAware, ProgressIndicationAware,
        ConnectivityAware, TrackingAware, ApiErrorAware, LaunchProductListAware,
        AnalyticsNavigationContextAware {

    protected BigBasketMessageHandler handler;
    private ProgressDialog progressDialog;
    protected BasketOperationResponse basketOperationResponse;
    private String mNavigationContext;
    private String mNextScreenNavigationContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = new BigBasketMessageHandler<>(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null) {
            getView().setClickable(true);
        }
        if (getCurrentActivity() != null && getCurrentActivity().getSupportActionBar() != null) {
            getCurrentActivity().getSupportActionBar().setSubtitle(null);
        }
        mNavigationContext = getArguments() != null ?
                getArguments().getString(TrackEventkeys.NAVIGATION_CTX) : null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getCurrentActivity() != null)
            MoEngageWrapper.onFragmentStart(getCurrentActivity().getMoEHelper(), getCurrentActivity(),
                     this.getClass().getSimpleName());
    }

    @Override
    public void onStop() {
        super.onStop();
        if(getCurrentActivity() != null)
            MoEngageWrapper.onFragmentStop(getCurrentActivity().getMoEHelper(), getCurrentActivity(),
                    this.getClass().getSimpleName());
    }

    @Override
    public void onBackResume() {
        super.onBackResume();
        setTitle();
        if (getCurrentActivity() != null && getCurrentActivity().isBasketDirty()) {
            syncBasket();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseActivity) {
            ((BaseActivity) activity).triggerActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Nullable
    @Override
    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void showProgressView() {
        if (getActivity() == null) return;
        ViewGroup view = getContentView();
        if (view == null) return;
        view.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View loadingView = inflater.inflate(R.layout.uiv3_loading_layout, view, false);
        view.addView(loadingView);
    }

    public void hideProgressView() {
        if (getActivity() == null) return;
        ViewGroup view = getContentView();
        if (view == null) return;
        view.removeAllViews();
    }

    public void showProgressDialog(String msg) {
        showProgressDialog(msg, true);
    }

    @Override
    public void showProgressDialog(String msg, boolean cancelable) {
        showProgressDialog(msg, cancelable, false);
    }

    @Override
    public void showProgressDialog(String msg, boolean cancelable, boolean isDeterminate) {
        if (progressDialog != null && progressDialog.isShowing()) return;
        if (TextUtils.isEmpty(msg)) {
            msg = getResources().getString(R.string.please_wait);
        }
        progressDialog = new ProgressDialog(getActivity());
        if (isDeterminate) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                progressDialog.setProgressNumberFormat(null);
                progressDialog.setProgressPercentFormat(null);
            }
        }
        progressDialog.setCancelable(cancelable);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()
                && !isSuspended()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public boolean checkInternetConnection() {
        return getActivity() != null && ((BaseActivity) getActivity()).checkInternetConnection();
    }

    @Override
    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    public void changeFragment(AbstractFragment newFragment) {
        if (getCurrentActivity() == null) return;
        setSuspended(true);
        getCurrentActivity().onChangeFragment(newFragment);
    }

    private void changeTitle(String title) {
        if (getCurrentActivity() != null && title != null && !isSuspended()) {
            getCurrentActivity().onChangeTitle(title);
        }
    }

    public void setTitle() {
        changeTitle(getTitle());
    }

    /**
     * Return null if you don't want the title to be changed
     */
    public abstract String getTitle();

    public void setTitle(String title) {
        changeTitle(title);
    }

    public void showErrorMsg(String msg) {
        if (getCurrentActivity() != null) {
            getCurrentActivity().showAlertDialog(msg);
        }
    }

    @Nullable
    public abstract ViewGroup getContentView();

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

    @Override
    public void setBasketOperationResponse(BasketOperationResponse basketOperationResponse) {
        this.basketOperationResponse = basketOperationResponse;
    }

    @Override
    public void updateUIAfterBasketOperationFailed(@BasketOperation.Mode int basketOperation, TextView basketCountTextView,
                                                   View viewDecQty, View viewIncQty, View btnAddToBasket,
                                                   Product product, String qty, String errorType,
                                                   @Nullable View productView,
                                                   @Nullable EditText editTextQty) {
        if (errorType.equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(getActivity(), "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, View btnAddToBasket,
                                                    Product product, String qty,
                                                    @Nullable View productView, @Nullable HashMap<String, Integer> cartInfoMap,
                                                    @Nullable EditText editTextQty) {

        int productQtyInBasket = 0;
        if (basketOperationResponse.getBasketResponseProductInfo() != null) {
            productQtyInBasket = Integer.parseInt(basketOperationResponse.getBasketResponseProductInfo().getTotalQty());
        }
        int totalProductsInBasket = basketOperationResponse.getCartSummary().getNoOfItems();

        if (productQtyInBasket == 0) {
            if (viewDecQty != null) {
                viewDecQty.setVisibility(View.GONE);
            }
            if (viewIncQty != null) {
                viewIncQty.setVisibility(View.GONE);
            }
            if (btnAddToBasket != null) {
                btnAddToBasket.setVisibility(View.VISIBLE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setVisibility(View.GONE);
            }
            if (productView != null) {
                productView.setBackgroundColor(Color.WHITE);
            }
            if (editTextQty != null && AuthParameters.getInstance(getCurrentActivity()).isKirana()) {
                editTextQty.setText("1");
                editTextQty.setVisibility(View.VISIBLE);
            }
        } else {
            if (viewDecQty != null) {
                viewDecQty.setVisibility(View.VISIBLE);
            }
            if (viewIncQty != null) {
                viewIncQty.setVisibility(View.VISIBLE);
            }
            if (btnAddToBasket != null) {
                btnAddToBasket.setVisibility(View.GONE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setText(String.valueOf(productQtyInBasket));
                basketCountTextView.setVisibility(View.VISIBLE);
            }
            if (editTextQty != null && AuthParameters.getInstance(getCurrentActivity()).isKirana()) {
                editTextQty.setVisibility(View.GONE);
            }
        }
        if (product != null) {
            product.setNoOfItemsInCart(productQtyInBasket);
            if (cartInfoMap != null) {
                cartInfoMap.put(product.getSku(), productQtyInBasket);
            }
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(Constants.GET_CART, String.valueOf(totalProductsInBasket));
        editor.apply();
        if (getCartSummary() != null) {
            getCartSummary().setNoOfItems(totalProductsInBasket);
        }
    }

    @Override
    public CartSummary getCartSummary() {
        if (getCurrentActivity() instanceof CartInfoAware) {
            return ((CartInfoAware) getCurrentActivity()).getCartSummary();
        }
        return null;
    }

    @Override
    public void setCartSummary(CartSummary cartInfo) {
        if (getCurrentActivity() instanceof CartInfoAware) {
            ((CartInfoAware) getCurrentActivity()).setCartSummary(cartInfo);
        }
    }

    @Override
    public void updateUIForCartInfo() {
        if (getCurrentActivity() != null && getCurrentActivity() instanceof CartInfoAware) {
            ((CartInfoAware) getCurrentActivity()).updateUIForCartInfo();
        }
    }

    @Override
    public void markBasketDirty() {
        if (getCurrentActivity() != null && getCurrentActivity() instanceof CartInfoAware) {
            ((CartInfoAware) getCurrentActivity()).markBasketDirty();
        }
    }

    @Override
    public void syncBasket() {
        if (getCurrentActivity() != null && getCurrentActivity() instanceof CartInfoAware) {
            ((CartInfoAware) getCurrentActivity()).syncBasket();
        }
    }

    public void showAlertDialog(String title,
                                String msg, @DialogButton.ButtonType int dialogButton,
                                @DialogButton.ButtonType int nxtDialogButton, final String sourceName,
                                final Object passedValue, String positiveBtnText) {
        if (getActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(!TextUtils.isEmpty(title) ? title : "BigBasket");
        builder.setMessage(msg);
        builder.setCancelable(false);
        if (dialogButton != DialogButton.NONE) {
            if (dialogButton == DialogButton.YES || dialogButton == DialogButton.OK) {
                if (TextUtils.isEmpty(positiveBtnText)) {
                    int textId = dialogButton == DialogButton.YES ? R.string.yesTxt : R.string.ok;
                    positiveBtnText = getString(textId);
                }
                builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onPositiveButtonClicked(dialogInterface, sourceName, passedValue);
                    }
                });
            }
            if (nxtDialogButton != DialogButton.NONE && nxtDialogButton == DialogButton.NO
                    || nxtDialogButton == DialogButton.CANCEL) {
                builder.setNegativeButton(R.string.noTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onNegativeButtonClicked(dialogInterface, sourceName);
                    }
                });
            }
        }
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.setOnShowListener(new OnDialogShowListener());
        alertDialog.show();
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, @Nullable String sourceName, Object valuePassed) {
        if (sourceName != null && getActivity() != null) {
            switch (sourceName) {
                case NavigationCodes.GO_TO_LOGIN:
                    Intent loginIntent = new Intent(getActivity(), SignInActivity.class);
                    startActivityForResult(loginIntent, NavigationCodes.GO_TO_HOME);
                    break;
                case Constants.NOT_ALPHANUMERIC_TXT_SHOPPING_LIST:
                    new CreateShoppingListTask<>(this).showDialog();
            }
        }
    }

    protected void onNegativeButtonClicked(DialogInterface dialogInterface, String sourceName) {

    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs,
                           String source, String sourceValue, boolean isCustomerValueIncrease,
                           boolean sendToFacebook) {
        trackEvent(eventName, eventAttribs, source, sourceValue, getCurrentNavigationContext(),
                isCustomerValueIncrease, sendToFacebook);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs) {
        if (getCurrentActivity() == null) return;
        trackEvent(eventName, eventAttribs, null, null);
    }

    @Override
    public void trackEventAppsFlyer(String eventName) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().trackEventAppsFlyer(eventName);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source, String sourceValue) {
        if (getCurrentActivity() == null) return;
        trackEvent(eventName, eventAttribs, source, sourceValue, getCurrentNavigationContext(), false, false);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source,
                           String sourceValue, boolean isCustomerValueIncrease) {
        if (getCurrentActivity() == null) return;
        trackEvent(eventName, eventAttribs, source, sourceValue, getCurrentNavigationContext(),
                isCustomerValueIncrease, false);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source,
                           String sourceValue, String nc, boolean isCustomerValueIncrease,
                           boolean sendToFacebook) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().trackEvent(eventName, eventAttribs, source, sourceValue,
                nc, isCustomerValueIncrease, sendToFacebook);
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().showAlertDialog(title, message);
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, boolean finish) {
        // Fix this implementation as fragment shouldn't finish activity
        if (getCurrentActivity() == null) return;
        if (finish) {
            showAlertDialogFinish(title, message);
        } else {
            getCurrentActivity().showAlertDialog(title, message);
        }
    }

    public void showAlertDialogFinish(String title, String msg) {
        if (getCurrentActivity() == null || isSuspended()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
        builder.setTitle(title == null ? "BigBasket" : title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getCurrentActivity() != null) {
                    finish();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.show();
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, String sourceName, Object valuePassed) {
        if (getCurrentActivity() == null) return;
        showAlertDialog(title, message, DialogButton.OK, DialogButton.NONE, sourceName, valuePassed, null);
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, int resultCode) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().showAlertDialogFinish(title, message, resultCode);
    }

    public abstract String getScreenTag();

    @Nullable
    @Override
    public String getCurrentNavigationContext() {
        if (mNavigationContext == null && getActivity() != null && ((BaseActivity) getActivity()).getCurrentNavigationContext() != null)
            return ((BaseActivity) getActivity()).getCurrentNavigationContext();
        return mNavigationContext;
    }

    @Override
    public void setCurrentNavigationContext(@Nullable String nc) {
        mNavigationContext = nc;
    }

    @Nullable
    @Override
    public String getNextScreenNavigationContext() {
        if (mNextScreenNavigationContext == null && getActivity() != null && ((BaseActivity) getActivity()).getNextScreenNavigationContext() != null)
            return ((BaseActivity) getActivity()).getNextScreenNavigationContext();
        return mNextScreenNavigationContext;
    }

    @Override
    public void setNextScreenNavigationContext(@Nullable String nc) {
        mNextScreenNavigationContext = nc;
        if (getCurrentActivity() != null) {
            getCurrentActivity().setNextScreenNavigationContext(mNextScreenNavigationContext);
        }
    }

    public void onResume() {
        super.onResume();
        setTitle();
        LocalyticsWrapper.tagScreen(getScreenTag());
    }

    @Override
    public void launchProductList(ArrayList<NameValuePair> nameValuePairs,
                                  @Nullable String sectionName, @Nullable String sectionItemName) {
        if (getActivity() != null) {
            ((LaunchProductListAware) getActivity()).
                    launchProductList(nameValuePairs, sectionName, sectionItemName);
        }
    }

    @Override
    public void launchShoppingList(ShoppingListName shoppingListName) {
        Intent intent = new Intent(getCurrentActivity(), ShoppingListSummaryActivity.class);
        intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
        super.startActivityForResult(intent, requestCode);
    }
}