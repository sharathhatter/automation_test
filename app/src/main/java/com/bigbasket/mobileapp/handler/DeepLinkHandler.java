package com.bigbasket.mobileapp.handler;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.bigbasket.mobileapp.activity.CustomerFeedbackActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.DoWalletActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeepLinkHandler {
    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int LOGIN_REQUIRED = 3;

    public static int handleDeepLink(ActivityAware context, Uri uri) {
        if (uri == null) {
            return FAILED;
        }

        AuthParameters authParameters = AuthParameters.getInstance(context.getCurrentActivity());
        if (getLoginRequiredUrls().contains(uri.getHost()) && authParameters.isAuthTokenEmpty()) {
            return LOGIN_REQUIRED;
        }
        UtmHandler.postUtm(context.getCurrentActivity(), uri);
        switch (uri.getHost()) {
            case Constants.PROMO:
                String id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id)) {
                    Intent intent = new Intent(context.getCurrentActivity(), BBActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
                    intent.putExtra(Constants.PROMO_ID, Integer.parseInt(id));
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.FEEDBACK:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    Intent intent = new Intent(context.getCurrentActivity(), CustomerFeedbackActivity.class);
                    intent.putExtra(Constants.CASE_ID, id);
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.ORDER:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(context.getCurrentActivity());
                    ((ProgressIndicationAware) context).showProgressDialog("Please wait!");
                    bigBasketApiService.getInvoice(id, new CallbackOrderInvoice<>(context.getCurrentActivity()));
                    return SUCCESS;
                }
                return FAILED;
            case Constants.WALLET:
                Intent intent = new Intent(context.getCurrentActivity(), DoWalletActivity.class);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            case Constants.PRODUCT_LIST:
                String queryParams = uri.getQuery();
                if (!TextUtils.isEmpty(queryParams)) {
                    ArrayList<NameValuePair> nameValuePairs = UIUtil.getProductQueryParams(queryParams);
                    context.getCurrentActivity().launchProductList(nameValuePairs, null, null);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.CATEGORY_LANDING:
                String name = uri.getQueryParameter(Constants.NAME);
                String slug = uri.getQueryParameter(Constants.SLUG);
                if (!TextUtils.isEmpty(slug)) {
                    intent = new Intent(context.getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CATEGORY_LANDING);
                    intent.putExtra(Constants.TOP_CATEGORY_SLUG, slug);
                    intent.putExtra(Constants.TOP_CATEGORY_NAME, name);
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.PRODUCT_DETAIL:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    intent = new Intent(context.getCurrentActivity(), BackButtonWithBasketButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                    intent.putExtra(Constants.SKU_ID, id);
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.ORDER_ITEMS:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    intent = new Intent(context.getCurrentActivity(), BackButtonWithBasketButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_PRODUCT_LIST_FRAGMENT);
                    intent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_DEEP_LINK);
                    intent.putExtra(Constants.ORDER_ID, id);
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.ALL_SL:
                id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id)) {
                    intent = new Intent(context.getCurrentActivity(), ShoppingListActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    return SUCCESS;
                }
                return FAILED;
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
                boolean isLoginRequiredToViewSl = authParameters.isAuthTokenEmpty() && (!isSystem
                        || (slug != null && slug.equalsIgnoreCase(Constants.SMART_BASKET_SLUG)));
                if (isLoginRequiredToViewSl) {
                    return LOGIN_REQUIRED;
                }
                if (!TextUtils.isEmpty(slug)) {
                    ShoppingListName shoppingListName = new ShoppingListName(name, slug, isSystem);
                    intent = new Intent(context.getCurrentActivity(), ShoppingListSummaryActivity.class);
                    intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.PROMO_LIST:
                intent = new Intent(context.getCurrentActivity(), BBActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_CATEGORY);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            case Constants.SMART_BASKET_SLUG:
                ShoppingListName shoppingListName = new ShoppingListName(Constants.SMART_BASKET,
                        Constants.SMART_BASKET_SLUG, true);
                intent = new Intent(context.getCurrentActivity(), ShoppingListSummaryActivity.class);
                intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            case Constants.BASKET:
                context.getCurrentActivity().launchViewBasketScreen();
                return SUCCESS;
            case Constants.FLAT_PAGE:
                String url = uri.getQueryParameter("url");
                try {
                    if (!TextUtils.isEmpty(url)) {
                        intent = new Intent(context.getCurrentActivity(), FlatPageWebViewActivity.class);
                        intent.putExtra(Constants.WEBVIEW_URL, URLDecoder.decode(url, "UTF-8"));
                        context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        return SUCCESS;
                    }
                } catch (UnsupportedEncodingException e) {
                }
                return FAILED;
            case Constants.DYNAMIC_PAGE:
                String screenName = uri.getQueryParameter(Constants.SCREEN);
                intent = new Intent(context.getCurrentActivity(), BBActivity.class);
                intent.putExtra(Constants.SCREEN, screenName);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_DYNAMIC_SCREEN);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            case Constants.AUTH:
                if (authParameters.isAuthTokenEmpty()) {
                    context.getCurrentActivity().launchLogin(TrackEventkeys.NAVIGATION_CTX_DEEP_LINK);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.INBOX:
                if (!authParameters.isAuthTokenEmpty()) {
                    context.getCurrentActivity().launchMoEngageCommunicationHub();
                    return SUCCESS;
                }
                return FAILED;
            default:
                return FAILED;
        }
    }

    private static Set<String> getLoginRequiredUrls() {
        Set<String> loginRequiredUrls = new HashSet<>();
        loginRequiredUrls.add(Constants.PROMO);
        loginRequiredUrls.add(Constants.FEEDBACK);
        loginRequiredUrls.add(Constants.ORDER);
        loginRequiredUrls.add(Constants.WALLET);
        loginRequiredUrls.add(Constants.ORDER_ITEMS);
        loginRequiredUrls.add(Constants.ALL_SL);
        loginRequiredUrls.add(Constants.SMART_BASKET_SLUG);
        loginRequiredUrls.add(Constants.INBOX);
        return loginRequiredUrls;
    }
}
