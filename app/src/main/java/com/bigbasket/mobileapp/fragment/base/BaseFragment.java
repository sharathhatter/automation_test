package com.bigbasket.mobileapp.fragment.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.bigbasket.mobileapp.fragment.dialogs.ConfirmationDialogFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.AnalyticsNavigationContextAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.interfaces.OnBasketChangeListener;
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
import com.bigbasket.mobileapp.util.LeakCanaryObserver;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseFragment extends AbstractFragment implements
        CartInfoAware, BasketOperationAware, TrackingAware, ApiErrorAware, LaunchProductListAware,
        AnalyticsNavigationContextAware, OnBasketChangeListener, ConfirmationDialogFragment.ConfirmationDialogCallback {

    protected BigBasketMessageHandler handler;
    protected BasketOperationResponse basketOperationResponse;
    private String mNavigationContext;
    private String mNextScreenNavigationContext;
    private String progressDialogTag;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        if (!isSuspended()) {
            // Don't use class.getName() in getInteractionName(), as with Proguard it returns obfuscated name
            NewRelic.setInteractionName(getActivity().getClass().getSimpleName() + "#" + getInteractionName());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getCurrentActivity() != null)
            MoEngageWrapper.onFragmentStart(getCurrentActivity().getMoEHelper(), getCurrentActivity(),
                    this.getClass().getSimpleName());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getCurrentActivity() != null)
            MoEngageWrapper.onFragmentStop(getCurrentActivity().getMoEHelper(), getCurrentActivity(),
                    this.getClass().getSimpleName());
    }

    @Override
    protected void onBackResume() {
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
        if (TextUtils.isEmpty(msg)) {
            msg = getResources().getString(R.string.please_wait);
        }
        String progressDialogTag = getProgressDialogTag();
        Fragment fragment = getFragmentManager().findFragmentByTag(progressDialogTag);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (fragment != null) {
            ft.remove(fragment);
        }
        fragment = ProgressDialogFragment.newInstance(msg, cancelable, isDeterminate);
        ft.add(fragment, progressDialogTag);
        if (!isSuspended()) {
            try {
                ft.commitAllowingStateLoss();
            } catch (IllegalStateException ex) {
                Crashlytics.logException(ex);
            }
        }
    }

    private String getProgressDialogTag() {
        if (progressDialogTag == null) {
            synchronized (this) {
                if (progressDialogTag == null) {
                    progressDialogTag = getScreenTag() + "#ProgressDilog";
                }
            }
        }
        return progressDialogTag;
    }

    public void hideProgressDialog() {
        String progressDialogTag = getProgressDialogTag();
        Fragment fragment = getFragmentManager().findFragmentByTag(progressDialogTag);
        if (fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            try {
                ft.remove(fragment);
            } finally {
                if (!isSuspended()) {
                    ft.commitAllowingStateLoss();
                }
            }
        }
    }

    public boolean checkInternetConnection() {
        return getActivity() != null && ((BaseActivity) getActivity()).checkInternetConnection();
    }

    @Override
    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    protected void changeFragment(AbstractFragment newFragment) {
        if (getCurrentActivity() == null) return;
        setSuspended(true);
        getCurrentActivity().onChangeFragment(newFragment);
    }

    private void changeTitle(String title) {
        if (getCurrentActivity() != null && title != null && !isSuspended()) {
            getCurrentActivity().onChangeTitle(title);
        }
    }

    protected void setTitle() {
        changeTitle(getTitle());
    }

    /**
     * Return null if you don't want the title to be changed
     */
    protected abstract String getTitle();

    protected void setTitle(String title) {
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

    protected Spannable asRupeeSpannable(double amt) {
        return UIUtil.asRupeeSpannable(amt, faceRupee);
    }

    protected Spannable asRupeeSpannable(String amtTxt) {
        return UIUtil.asRupeeSpannable(amtTxt, faceRupee);
    }

    @Override
    public void setBasketOperationResponse(BasketOperationResponse basketOperationResponse) {
        this.basketOperationResponse = basketOperationResponse;
    }

    @Override
    public void updateUIAfterBasketOperationFailed(@BasketOperation.Mode int basketOperation,
                                                   @Nullable WeakReference<TextView> basketCountTextViewRef,
                                                   @Nullable WeakReference<View> viewDecQtyRef,
                                                   @Nullable WeakReference<View> viewIncQtyRef,
                                                   @Nullable WeakReference<View> btnAddToBasketRef,
                                                   Product product, String qty, String errorType,
                                                   @Nullable WeakReference<View> productViewRef,
                                                   @Nullable WeakReference<EditText> editTextQtyRef) {
        if (errorType.equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(getActivity(), "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation,
                                                    @Nullable WeakReference<TextView> basketCountTextViewRef,
                                                    @Nullable WeakReference<View> viewDecQtyRef,
                                                    @Nullable WeakReference<View> viewIncQtyRef,
                                                    @Nullable WeakReference<View> btnAddToBasketRef,
                                                    Product product, String qty,
                                                    @Nullable WeakReference<View> productViewRef,
                                                    @Nullable WeakReference<HashMap<String, Integer>> cartInfoMapRef,
                                                    @Nullable WeakReference<EditText> editTextQtyRef) {

        int productQtyInBasket = 0;
        if (basketOperationResponse.getBasketResponseProductInfo() != null) {
            productQtyInBasket = Integer.parseInt(basketOperationResponse.getBasketResponseProductInfo().getTotalQty());
        }
        int totalProductsInBasket = basketOperationResponse.getCartSummary().getNoOfItems();

        if (productQtyInBasket == 0) {
            if (viewDecQtyRef != null && viewDecQtyRef.get() != null) {
                viewDecQtyRef.get().setVisibility(View.GONE);
            }
            if (viewIncQtyRef != null && viewIncQtyRef.get() != null) {
                viewIncQtyRef.get().setVisibility(View.GONE);
            }
            if (btnAddToBasketRef != null && btnAddToBasketRef.get() != null) {
                btnAddToBasketRef.get().setVisibility(View.VISIBLE);
            }
            if (basketCountTextViewRef != null && basketCountTextViewRef.get() != null) {
                basketCountTextViewRef.get().setVisibility(View.GONE);
            }
            if (productViewRef != null && productViewRef.get() != null) {
                productViewRef.get().setBackgroundColor(Color.WHITE);
            }
            if (editTextQtyRef != null && editTextQtyRef.get() != null
                    && AuthParameters.getInstance(getCurrentActivity()).isKirana()) {
                editTextQtyRef.get().setText("1");
                editTextQtyRef.get().setVisibility(View.VISIBLE);
            }
        } else {
            if (viewDecQtyRef != null && viewDecQtyRef.get() != null) {
                viewDecQtyRef.get().setVisibility(View.VISIBLE);
            }
            if (viewIncQtyRef != null && viewIncQtyRef.get() != null) {
                viewIncQtyRef.get().setVisibility(View.VISIBLE);
            }
            if (btnAddToBasketRef != null && btnAddToBasketRef.get() != null) {
                btnAddToBasketRef.get().setVisibility(View.GONE);
            }
            if (basketCountTextViewRef != null && basketCountTextViewRef.get() != null) {
                basketCountTextViewRef.get().setText(String.valueOf(productQtyInBasket));
                basketCountTextViewRef.get().setVisibility(View.VISIBLE);
            }
            if (editTextQtyRef != null && editTextQtyRef.get() != null
                    && AuthParameters.getInstance(getCurrentActivity()).isKirana()) {
                editTextQtyRef.get().setVisibility(View.GONE);
            }
        }
        if (product != null) {
            product.setNoOfItemsInCart(productQtyInBasket);
            if (cartInfoMapRef != null && cartInfoMapRef.get() != null) {
                cartInfoMapRef.get().put(product.getSku(), productQtyInBasket);
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
                                @DialogButton.ButtonType int nxtDialogButton, final int requestCode,
                                final Bundle passedValue, String positiveBtnText) {
        if (getActivity() == null) return;
        String negativeButtonText = null;

        if (dialogButton != DialogButton.NONE &&
                (dialogButton == DialogButton.YES || dialogButton == DialogButton.OK)) {
            if (TextUtils.isEmpty(positiveBtnText)) {
                int textId = dialogButton == DialogButton.YES ? R.string.yesTxt : R.string.ok;
                positiveBtnText = getString(textId);
            }
        }
        if (nxtDialogButton != DialogButton.NONE &&
                (nxtDialogButton == DialogButton.NO || nxtDialogButton == DialogButton.CANCEL)) {
            int textId = nxtDialogButton == DialogButton.NO ? R.string.noTxt : R.string.cancel;
            negativeButtonText = getString(textId);
        }
        if (isSuspended())
            return;
        // Defensive check
        if (TextUtils.isEmpty(positiveBtnText) && TextUtils.isEmpty(negativeButtonText)) {
            positiveBtnText = getString(R.string.ok);
        }
        ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(this,
                requestCode, title == null ? getString(R.string.app_name) : title, msg, positiveBtnText,
                negativeButtonText, passedValue, false);
        try {
            dialogFragment.show(getFragmentManager(), getScreenTag() + "#AlertDialog");
        } catch (IllegalStateException ex) {
            Crashlytics.logException(ex);
        }
    }

    @Override
    public void onDialogConfirmed(int reqCode, Bundle data, boolean isPositive) {
        if (getCurrentActivity() == null) return;
        if (isPositive) {
            onPositiveButtonClicked(reqCode, data);
            if (data != null && data.getBoolean(Constants.FINISH_ACTIVITY, false)) {
                getCurrentActivity().finish();
            }
        } else {
            onNegativeButtonClicked(reqCode);
        }
    }


    @Override
    public void onDialogCancelled(int reqCode) {

    }

    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        if (getActivity() != null) {
            switch (sourceName) {
                case NavigationCodes.GO_TO_LOGIN:
                    Intent loginIntent = new Intent(getActivity(), SignInActivity.class);
                    startActivityForResult(loginIntent, NavigationCodes.GO_TO_HOME);
                    break;
                case Constants.NOT_ALPHANUMERIC_TXT_SHOPPING_LIST_DIALOG:
                    new CreateShoppingListTask<>(this).showDialog();
            }
        }
    }

    protected void onNegativeButtonClicked(int requestCode) {

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

        Bundle data = new Bundle(2);
        data.putBoolean(Constants.FINISH_ACTIVITY, true);
        ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(
                this, 0, title == null ? getString(R.string.app_name) : title, msg,
                getString(R.string.ok),
                null, data, false);
        try {
            dialogFragment.show(getFragmentManager(), getScreenTag() + "#AlertDialog");
        } catch (IllegalStateException ex) {
            Crashlytics.logException(ex);
        }
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, int requestCode, Bundle valuePassed) {
        if (getCurrentActivity() == null) return;
        showAlertDialog(title, message, DialogButton.OK, DialogButton.NONE, requestCode, valuePassed, null);
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

    @Override
    public void onBasketChanged(@Nullable Intent data) {
        if (getActivity() == null) return;
        ((BaseActivity) getActivity()).onBasketChanged(data);
    }

    @Override
    public void markBasketChanged(@Nullable Intent data) {
        if (getActivity() == null) return;
        ((BaseActivity) getActivity()).markBasketChanged(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        observeMemoryLeak();
    }

    protected void observeMemoryLeak() {
        LeakCanaryObserver.Factory.observe(this);
    }

    @NonNull
    public abstract String getInteractionName();  // Don't use class.getName(), as with Proguard it returns obfuscated value
}