package com.bigbasket.mobileapp.handler;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.bigbasket.mobileapp.activity.CustomerFeedbackActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.DoWalletActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.SearchActivity;
import com.bigbasket.mobileapp.activity.payment.FundWalletActivity;
import com.bigbasket.mobileapp.activity.payment.PayNowActivity;
import com.bigbasket.mobileapp.activity.product.DiscountActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FlatPageHelper;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import retrofit2.Call;

public class DeepLinkHandler {
    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int LOGIN_REQUIRED = 3;
    public static final int REGISTER_DEVICE_REQUIRED = 4;
    public static final String PATH_MY_WALLET = "/member/credit/";
    public static final String PATH_FUND_WALLET = "/payment/wallet/";
    public static final String PATH_MY_ORDERS = "/member/active-orders/";
    public static final String REGEX_PATH_MY_WALLET = "^" + PATH_MY_WALLET + "$";
    public static final String REGEX_PATH_FUND_WALLET = "^" + PATH_FUND_WALLET + "$";
    public static final String REGEX_PATH_PAY_NOW = "^/payment/pay_now/\\d+/$";
    public static final String REGEX_PATH_MY_ORDERS = "^" + PATH_MY_ORDERS + "$";

    public static int handleDeepLink(AppOperationAware context, Uri uri) {
        if (uri == null) {
            return FAILED;
        }

        AuthParameters authParameters = AuthParameters.getInstance(context.getCurrentActivity());

        /**
         * checking if the visitorId is empty/null
         * empty/null means the app hasn't registered the device with server
         */
        if (TextUtils.isEmpty(authParameters.getVisitorId())) {
            return REGISTER_DEVICE_REQUIRED;
        }
        if (authParameters.isAuthTokenEmpty()) {
            if (uri.getHost().contains(Constants.HTTP_HOST)) {
                String path = uri.getPath();
                if (!TextUtils.isEmpty(path)) {
                    for (Iterator<String> iterator = getHttpLoginRequiredUrls().iterator(); iterator.hasNext(); ) {
                        String pathPattern = iterator.next();
                        if (path.matches(pathPattern)) {
                            return LOGIN_REQUIRED;
                        }
                    }
                } else return FAILED;
            }
            if (getLoginRequiredUrls().contains(uri.getHost())) {
                return LOGIN_REQUIRED;
            }
        }

        UtmHandler.postUtm(context.getCurrentActivity(), uri);
        if (uri.getHost().endsWith(Constants.HTTP_HOST)) {
            return handleHttpLinks(context, uri);
        }
        switch (uri.getHost()) {
            case Constants.PROMO:
                String id = uri.getQueryParameter(Constants.ID);
                if (!TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id)) {
                    Intent intent = new Intent(context.getCurrentActivity(), BackButtonActivity.class);
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
                    context.showProgressDialog("Please wait!");
                    Call<ApiResponse<OrderInvoice>> call =
                            bigBasketApiService.getInvoice(context.getCurrentActivity().getPreviousScreenName(), id);
                    call.enqueue(new CallbackOrderInvoice<>(context));
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
                intent = new Intent(context.getCurrentActivity(), ShoppingListActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            case Constants.SL_SUMMARY:
                slug = uri.getQueryParameter(Constants.SLUG);
                name = uri.getQueryParameter(Constants.NAME);
                boolean isSystem;
                isSystem = uri.getBooleanQueryParameter(Constants.SHOPPING_LIST_IS_SYSTEM,
                        false);
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
                intent = new Intent(context.getCurrentActivity(), BackButtonActivity.class);
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
                        FlatPageHelper.openFlatPage(context.getCurrentActivity(),
                                URLDecoder.decode(url, "UTF-8"), null);
                        return SUCCESS;
                    }
                } catch (UnsupportedEncodingException e) {
                }
                return FAILED;
            case Constants.DYNAMIC_PAGE:
                String screenName = uri.getQueryParameter(Constants.SCREEN);
                intent = new Intent(context.getCurrentActivity(), SearchActivity.class);
                intent.putExtra(Constants.SCREEN, screenName);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_DYNAMIC_SCREEN);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            case Constants.AUTH:
                if (authParameters.isAuthTokenEmpty()) {
                    context.getCurrentActivity().launchLogin(TrackEventkeys.NAVIGATION_CTX_DEEP_LINK, true);
                    return SUCCESS;
                }
                return FAILED;
            case Constants.INBOX:
                if (!authParameters.isAuthTokenEmpty()) {
                    context.getCurrentActivity().launchMoEngageCommunicationHub();
                    return SUCCESS;
                }
                return LOGIN_REQUIRED;
            case Constants.DISCOUNT:
                intent = new Intent(context.getCurrentActivity(), DiscountActivity.class);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            case Constants.HOME:
                context.getCurrentActivity().goToHome();
                return SUCCESS;
            case Constants.STORE_LIST:
                String category = uri.getQueryParameter(Constants.CATEGORY);
                if (!TextUtils.isEmpty(category) && !category.equalsIgnoreCase("null")) {
                    context.getCurrentActivity().launchStoreList(category);
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

    private static int handleHttpLinks(AppOperationAware context, Uri uri) {
        String path = uri.getPath();
        if (!TextUtils.isEmpty(path)) {
            if (path.equalsIgnoreCase(PATH_MY_WALLET)) {
                Intent intent = new Intent(context.getCurrentActivity(), DoWalletActivity.class);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            } else if (path.equalsIgnoreCase(PATH_FUND_WALLET)) {
                Intent intent = new Intent(context.getCurrentActivity(), FundWalletActivity.class);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return SUCCESS;
            } else if (path.matches(REGEX_PATH_PAY_NOW)) {
                String orderId = uri.getLastPathSegment();
                if (!TextUtils.isEmpty(orderId) && !orderId.equalsIgnoreCase(Constants.PAY_NOW)) {
                    Intent intent = new Intent(context.getCurrentActivity(), PayNowActivity.class);
                    intent.putExtra(Constants.ORDER_ID, orderId);
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    return SUCCESS;
                }
            } else if (path.matches(REGEX_PATH_MY_ORDERS)) {
                Intent intent = new Intent(context.getCurrentActivity(), OrderListActivity.class);
                intent.putExtra(Constants.ORDER, "all");
                intent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.DEEP_LINK);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
            }
        }
        return FAILED;
    }

    private static Set<String> getHttpLoginRequiredUrls() {
        Set<String> loginRequiredUrls = new HashSet<>();
        loginRequiredUrls.add(REGEX_PATH_MY_WALLET);
        loginRequiredUrls.add(REGEX_PATH_FUND_WALLET);
        loginRequiredUrls.add(REGEX_PATH_PAY_NOW);
        loginRequiredUrls.add(REGEX_PATH_MY_ORDERS);
        return loginRequiredUrls;
    }
}
