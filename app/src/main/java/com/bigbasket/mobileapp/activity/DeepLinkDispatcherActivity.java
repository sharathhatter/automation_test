package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

public class DeepLinkDispatcherActivity extends BaseActivity implements InvoiceDataAware {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchCorrespondingActivity();
    }

    private void launchCorrespondingActivity() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }

        AuthParameters authParameters = AuthParameters.getInstance(this);
        if (getLoginRequiredUrls().contains(uri.getHost()) && authParameters.isAuthTokenEmpty()) {
            showAlertDialog(null, getString(R.string.login_required), NavigationCodes.GO_TO_LOGIN, uri);
            return;
        }
        switch (uri.getHost()) {
            case Constants.HOME:
                navigateToHome(true);
                break;
            case Constants.PROMO:
                String id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id)) {
                    Intent intent = new Intent(this, BBActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
                    intent.putExtra(Constants.PROMO_ID, Integer.parseInt(id));
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                } else {
                    showDefaultError();
                }
                break;
            case Constants.FEEDBACK:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    Intent intent = new Intent(this, CustomerFeedbackActivity.class);
                    intent.putExtra(Constants.CASE_ID, id);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                } else {
                    showDefaultError();
                }
                break;
            case Constants.ORDER:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
                    showProgressDialog(getString(R.string.please_wait));
                    bigBasketApiService.getInvoice(id, new CallbackOrderInvoice<>(this));
                } else {
                    showDefaultError();
                }
                break;
            case Constants.WALLET:
                Intent intent = new Intent(this, BackButtonActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WALLET_FRAGMENT);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case Constants.CATEGORY_LANDING:
                String name = uri.getQueryParameter(Constants.NAME);
                String slug = uri.getQueryParameter(Constants.SLUG);
                if (!TextUtils.isEmpty(slug)) {
                    intent = new Intent(this, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CATEGORY_LANDING);
                    intent.putExtra(Constants.TOP_CATEGORY_SLUG, slug);
                    intent.putExtra(Constants.TOP_CATEGORY_NAME, name);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                } else {
                    showDefaultError();
                }
                break;
            case Constants.PRODUCT_DETAIL:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    intent = new Intent(this, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                    intent.putExtra(Constants.SKU_ID, id);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                } else {
                    showDefaultError();
                }
                break;
            case Constants.ORDER_ITEMS:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    intent = new Intent(this, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_PRODUCT_LIST_FRAGMENT);
                    intent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_DEEP_LINK);
                    intent.putExtra(Constants.ORDER_ID, id);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                } else {
                    showDefaultError();
                }
                break;
            case Constants.ALL_SL:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    intent = new Intent(this, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                } else {
                    showDefaultError();
                }
                break;
            case Constants.SL_SUMMARY:
                slug = uri.getQueryParameter(Constants.SLUG);
                name = uri.getQueryParameter(Constants.NAME);
                boolean isSystem;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    isSystem = uri.getBooleanQueryParameter(Constants.SHOPPING_LIST_IS_SYSTEM,
                            false);
                } else {
                    String isSystemStr = uri.getQueryParameter(Constants.SHOPPING_LIST_IS_SYSTEM);
                    isSystem = !TextUtils.isEmpty(isSystemStr) &&
                            !isSystemStr.equals("false") && !isSystemStr.equals("0");
                }
                if (!TextUtils.isEmpty(slug)) {
                    ShoppingListName shoppingListName = new ShoppingListName(name, slug, isSystem);
                    intent = new Intent(this, BackButtonActivity.class);
                    intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_SUMMARY);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                } else {
                    showDefaultError();
                }
                break;
            case Constants.SL_PRODUCTS:
                slug = uri.getQueryParameter(Constants.SLUG);
                name = uri.getQueryParameter(Constants.NAME);
                String categorySlug = uri.getQueryParameter(Constants.CATEGORY_SLUG);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    isSystem = uri.getBooleanQueryParameter(Constants.SHOPPING_LIST_IS_SYSTEM,
                            false);
                } else {
                    String isSystemStr = uri.getQueryParameter(Constants.SHOPPING_LIST_IS_SYSTEM);
                    isSystem = !TextUtils.isEmpty(isSystemStr) &&
                            !isSystemStr.equals("false") && !isSystemStr.equals("0");
                }
                if (!TextUtils.isEmpty(slug)) {
                    ShoppingListName shoppingListName = new ShoppingListName(name, slug, isSystem);
                    intent = new Intent(this, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_PRODUCTS);
                    intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                    intent.putExtra(Constants.TOP_CAT_SLUG, categorySlug);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                } else {
                    showDefaultError();
                }
                break;
            case Constants.PROMO_LIST:
                intent = new Intent(this, BBActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_CATEGORY);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case Constants.SMART_BASKET_SLUG:
                ShoppingListName shoppingListName = new ShoppingListName(Constants.SMART_BASKET,
                        Constants.SMART_BASKET_SLUG, true);
                intent = new Intent(this, BackButtonActivity.class);
                intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_SUMMARY);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case Constants.SMART_BASKET_PRODUCTS:
                categorySlug = uri.getQueryParameter(Constants.CATEGORY_SLUG);
                shoppingListName = new ShoppingListName(Constants.SMART_BASKET,
                        Constants.SMART_BASKET_SLUG, true);
                intent = new Intent(this, BackButtonActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_PRODUCTS);
                intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                intent.putExtra(Constants.TOP_CAT_SLUG, categorySlug);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case Constants.BASKET:
                intent = new Intent(this, BackButtonActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_BASKET);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case Constants.FLAT_PAGE:
                String url = uri.getQueryParameter("url");
                try {
                    if (!TextUtils.isEmpty(url)) {
                        intent = new Intent(this, FlatPageWebViewActivity.class);
                        intent.putExtra(Constants.WEBVIEW_URL, URLDecoder.decode(url, "UTF-8"));
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    } else {
                        showDefaultError();
                    }
                } catch (UnsupportedEncodingException e) {
                    showDefaultError();
                }
                break;
            case Constants.DYNAMIC_PAGE:
                String screenName = uri.getQueryParameter(Constants.SCREEN);
                intent = new Intent(this, BBActivity.class);
                intent.putExtra(Constants.SCREEN, screenName);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_DYNAMIC_SCREEN);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            default:
                finish();
                break;
        }
    }

    private Set<String> getLoginRequiredUrls() {
        Set<String> loginRequiredUrls = new HashSet<>();
        loginRequiredUrls.add(Constants.PROMO);
        loginRequiredUrls.add(Constants.FEEDBACK);
        loginRequiredUrls.add(Constants.ORDER);
        loginRequiredUrls.add(Constants.WALLET);
        loginRequiredUrls.add(Constants.ORDER_ITEMS);
        loginRequiredUrls.add(Constants.ALL_SL);
        loginRequiredUrls.add(Constants.SL_SUMMARY);
        loginRequiredUrls.add(Constants.SL_PRODUCTS);
        loginRequiredUrls.add(Constants.SMART_BASKET_SLUG);
        loginRequiredUrls.add(Constants.SMART_BASKET_PRODUCTS);
        return loginRequiredUrls;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        navigateToHome(false);
    }

    private void navigateToHome(boolean setHomeNavCode) {
        if (!getIntent().getBooleanExtra(Constants.HAS_PARENT, false)) {
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            finish();
        } else if (setHomeNavCode) {
            goToHome(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.GO_TO_HOME) {
            navigateToHome(true);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.DEEP_LINK_DISPATCHER_SCREEN;
    }

    private void showDefaultError() {
        showToast("Page Not Found");
        finish();
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_DEEP_LINK);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }
}