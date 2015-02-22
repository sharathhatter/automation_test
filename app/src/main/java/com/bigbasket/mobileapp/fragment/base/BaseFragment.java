package com.bigbasket.mobileapp.fragment.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.CheckoutQCActivity;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
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
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;

import java.text.NumberFormat;
import java.util.Map;


public abstract class BaseFragment extends AbstractFragment implements HandlerAware,
        CartInfoAware, BasketOperationAware, COReserveQuantityCheckAware, ProgressIndicationAware,
        ConnectivityAware, TrackingAware, ApiErrorAware {

    protected BigBasketMessageHandler handler;
    private ProgressDialog progressDialog;
    protected COReserveQuantity coReserveQuantity;

    private BasketOperationResponse basketOperationResponse;

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
        setTitle();
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
        setSuspended(true);
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseActivity) {
            ((BaseActivity) activity).triggerActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showProgressView() {
        if (getActivity() == null) return;
        LinearLayout view = getContentView();
        if (view == null) return;
        view.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View loadingView = inflater.inflate(R.layout.uiv3_loading_layout, view, false);
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
    public BigBasketMessageHandler getHandler() {
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
        if (getCurrentActivity() != null) {
            getCurrentActivity().showAlertDialog(msg);
        }
    }

    @Nullable
    public abstract LinearLayout getContentView();

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
                                                   View viewDecQty, View viewIncQty, Button btnAddToBasket,
                                                   EditText editTextQty, Product product, String qty, String errorType) {
        if (errorType.equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(getActivity(), "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, Button btnAddToBasket,
                                                    EditText editTextQty, Product product, String qty) {

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
            if (editTextQty != null) {
                editTextQty.setText("1");
                editTextQty.setVisibility(View.VISIBLE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setVisibility(View.GONE);
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
            if (editTextQty != null) {
                editTextQty.setVisibility(View.GONE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setText(productQtyInBasket + " in");
                basketCountTextView.setVisibility(View.VISIBLE);
            }
        }

        if (product != null) {
            product.setNoOfItemsInCart(productQtyInBasket);
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(Constants.GET_CART, String.valueOf(totalProductsInBasket));
        editor.commit();
        if (getCartInfo() != null) {
            getCartInfo().setNoOfItems(totalProductsInBasket);
        }
    }

    @Override
    public void setCartInfo(CartSummary cartInfo) {
        if (getCurrentActivity() instanceof CartInfoAware) {
            ((CartInfoAware) getCurrentActivity()).setCartInfo(cartInfo);
        }
    }

    @Override
    public CartSummary getCartInfo() {
        if (getCurrentActivity() instanceof CartInfoAware) {
            return ((CartInfoAware) getCurrentActivity()).getCartInfo();
        }
        return null;
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
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName,
                                final Object passedValue, String positiveBtnText) {
        if (getActivity() == null) return;
        MaterialDialog.Builder builder = UIUtil.getMaterialDialogBuilder(getActivity())
                .title(!TextUtils.isEmpty(title) ? title : "BigBasket")
                .content(msg);
        if (dialogButton != null) {
            if (dialogButton.equals(DialogButton.YES) || dialogButton.equals(DialogButton.OK)) {
                if (TextUtils.isEmpty(positiveBtnText)) {
                    int textId = dialogButton.equals(DialogButton.YES) ? R.string.yesTxt : R.string.ok;
                    positiveBtnText = getString(textId);
                }
                builder.positiveText(positiveBtnText);
            }
            if (nxtDialogButton != null && (nxtDialogButton.equals(DialogButton.NO)
                    || nxtDialogButton.equals(DialogButton.CANCEL))) {
                int textId = nxtDialogButton.equals(DialogButton.NO) ? R.string.noTxt : R.string.cancel;
                builder.negativeText(textId);
            }
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    onPositiveButtonClicked(dialog, sourceName, passedValue);
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    onNegativeButtonClicked(dialog, sourceName);
                }
            });
        }
        if (isSuspended())
            return;
        builder.show();
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, @Nullable String sourceName, Object valuePassed) {
        if (sourceName != null && getActivity() != null) {
            switch (sourceName) {
                case NavigationCodes.GO_TO_LOGIN:
                    Intent loginIntent = new Intent(getActivity(), SignInActivity.class);
                    startActivityForResult(loginIntent, NavigationCodes.GO_TO_HOME);
                    break;
            }
        }
    }

    protected void onNegativeButtonClicked(DialogInterface dialogInterface, String sourceName) {

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
            intent.putExtra(Constants.CO_RESERVE_QTY_DATA, coReserveQuantity);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
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
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source,
                           String sourceValue, boolean isCustomerValueIncrease) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().trackEvent(eventName, eventAttribs, source, sourceValue, false);
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

        MaterialDialog.Builder builder = UIUtil.getMaterialDialogBuilder(getCurrentActivity())
                .title(title == null ? "BigBasket" : title)
                .content(msg)
                .positiveText(R.string.ok)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (getCurrentActivity() != null) {
                            finish();
                        }
                    }
                });
        if (isSuspended())
            return;
        builder.show();
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, String sourceName, Object valuePassed) {
        if (getCurrentActivity() == null) return;
        showAlertDialog(title, message, DialogButton.OK, null, sourceName, valuePassed, null);
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, int resultCode) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().showAlertDialogFinish(title, message, resultCode);
    }

    public abstract String getScreenTag();

    public void onResume() {
        super.onResume();
        LocalyticsWrapper.onResume(getScreenTag());
    }
}