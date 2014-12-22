package com.bigbasket.mobileapp.fragment.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
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
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;

import java.text.NumberFormat;
import java.util.Map;


public abstract class BaseFragment extends AbstractFragment implements HandlerAware,
        CartInfoAware, BasketOperationAware, COReserveQuantityCheckAware, ProgressIndicationAware,
        ConnectivityAware, TrackingAware, ApiErrorAware {

    protected BigBasketMessageHandler handler;
    private ProgressDialog progressDialog;
    protected COReserveQuantity coReserveQuantity;

    private BasketOperationResponse basketOperationResponse;
    protected CartSummary cartInfo = new CartSummary();

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
                                                   EditText editTextQty, Product product, String qty, String errorType) {
        if (errorType.equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(getActivity(), "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    ImageView imgDecQty, ImageView imgIncQty, Button btnAddToBasket,
                                                    EditText editTextQty, Product product, String qty) {

        int productQtyInBasket = 0;
        if (basketOperationResponse.getBasketResponseProductInfo() != null) {
            productQtyInBasket = Integer.parseInt(basketOperationResponse.getBasketResponseProductInfo().getTotalQty());
        }
        int totalProductsInBasket = basketOperationResponse.getCartSummary().getNoOfItems();

        if (basketOperation == BasketOperation.INC) {
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

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
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

        AuthParameters.getInstance(getActivity()).setMoEngaleEnabled(cartInfo.isMoEngageEnabled(), getActivity());
        AuthParameters.updateInstance(getActivity());
    }

    public void showAlertDialog(String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName,
                                final Object passedValue, String positiveBtnText) {
        if (getActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(!TextUtils.isEmpty(title) ? title : "BigBasket");
        builder.setMessage(msg);
        if (dialogButton != null) {
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
            if (nxtDialogButton != null && nxtDialogButton.equals(DialogButton.NO))
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
            intent.putExtra(Constants.CO_RESERVE_QTY_DATA, coReserveQuantity);
            //intent.putExtra(Constants.QC_LEN, coReserveQuantity.getQc_len());
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
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source, String sourceValue) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().trackEvent(eventName, eventAttribs, source, sourceValue);
    }

    @Override
    public void showApiErrorDialog(String message) {
        if (getCurrentActivity() == null) return;
        showErrorMsg(message);
    }

    @Override
    public void showApiErrorDialog(String message, boolean finish) {
        if (getCurrentActivity() == null) return;
        if (finish) {
            getCurrentActivity().showAlertDialogFinish(null, message);
        } else {
            getCurrentActivity().showAlertDialog(message);
        }
    }

    @Override
    public void showApiErrorDialog(String message, String sourceName, Object valuePassed) {
        if (getCurrentActivity() == null) return;
        showAlertDialog(null, message, DialogButton.OK, null, sourceName, valuePassed, null);
    }

    @Override
    public void showApiErrorDialog(String message, int resultCode) {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().showAlertDialogFinish(null, message, resultCode);
    }
}