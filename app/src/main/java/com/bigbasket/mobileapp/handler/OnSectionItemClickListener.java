package com.bigbasket.mobileapp.handler;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OnSectionItemClickListener<T> implements View.OnClickListener, BaseSliderView.OnSliderClickListener {
    private T context;
    private Section section;
    private SectionItem sectionItem;

    public OnSectionItemClickListener(T context, Section section, SectionItem sectionItem) {
        this.context = context;
        this.section = section;
        this.sectionItem = sectionItem;
    }

    @Override
    public void onClick(View v) {
        onSectionClick();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        onSectionClick();
    }

    private void onSectionClick() {
        if (context == null || ((CancelableAware) context).isSuspended()) return;


        if (sectionItem.getDestinationInfo() != null &&
                sectionItem.getDestinationInfo().getDestinationType() != null) {
            DestinationInfo destinationInfo = sectionItem.getDestinationInfo();
            switch (destinationInfo.getDestinationType()) {
                case DestinationInfo.CATEGORY_LANDING:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CATEGORY_LANDING);
                        intent.putExtra(Constants.TOP_CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                        String title = sectionItem.getTitle() != null ? sectionItem.getTitle().getText() : "";
                        intent.putExtra(Constants.TOP_CATEGORY_NAME, title);
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    } else {
                        showDefaultError();
                    }
                    break;
                case DestinationInfo.FLAT_PAGE:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), FlatPageWebViewActivity.class);
                        intent.putExtra(Constants.WEBVIEW_URL, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    } else {
                        showDefaultError();
                    }
                    break;
                case DestinationInfo.PREVIOUS_ORDERS:
                    Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), OrderListActivity.class);
                    intent.putExtra(Constants.ORDER, ((ActivityAware) context).getCurrentActivity().getString(R.string.active_label));
                    intent.putExtra(Constants.SHOP_FROM_PREVIOUS_ORDER, true);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case DestinationInfo.PRODUCT_CATEGORY:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), ProductListActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_CATEGORY);
                        intent.putExtra(Constants.CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    } else {
                        showDefaultError();
                    }
                    break;
                case DestinationInfo.PRODUCT_DETAIL:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                        intent.putExtra(Constants.SKU_ID, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    } else {
                        showDefaultError();
                    }
                    break;
                case DestinationInfo.PROMO_DETAIL:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())
                            && TextUtils.isDigitsOnly(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BBActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
                        intent.putExtra(Constants.PROMO_ID, Integer.parseInt(destinationInfo.getDestinationSlug()));
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    } else {
                        showDefaultError();
                    }
                    break;
                case DestinationInfo.SHOPPING_LIST_SUMMARY:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        boolean isSmartBasket = destinationInfo.getDestinationSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG);
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        ShoppingListName shoppingListName = new ShoppingListName(destinationInfo.getDestinationSlug(), destinationInfo.getDestinationSlug(),
                                isSmartBasket);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_SUMMARY);
                        intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);

                        if(isSmartBasket) logMainMenuEvent(TrackingAware.SMART_BASKET_ICON_CLICKED,
                                TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_HOME_PAGE);
                        else logMainMenuEvent(TrackingAware.SHOPPING_LIST_ICON_CLICKED,
                                TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_HOME_PAGE);
                    } else {
                        showDefaultError();
                    }
                    break;
                case DestinationInfo.SHOPPING_LIST_LANDING:
                    intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case DestinationInfo.SEARCH:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), ProductListActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SEARCH);
                        intent.putExtra(Constants.SEARCH_QUERY, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    } else {
                        showDefaultError();
                    }
                    break;
                case DestinationInfo.PRODUCT_LIST:
                    launchProductList(destinationInfo);
                    break;
                case DestinationInfo.DEEP_LINK:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        try {
                            ((ActivityAware) context).getCurrentActivity().
                                    startActivityForResult(new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse(destinationInfo.getDestinationSlug())),
                                            NavigationCodes.GO_TO_HOME);
                        } catch (ActivityNotFoundException e) {
                            // Do nothing
                        }
                    } else {
                        showDefaultError();
                    }
                    break;
                case DestinationInfo.PROMO_LIST:
                    intent = new Intent(((ActivityAware) context).getCurrentActivity(), BBActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_CATEGORY);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case DestinationInfo.HOME:
                    ((ActivityAware) context).getCurrentActivity().goToHome();
                default:
                    showDefaultError();
                    break;
            }
        } else {
            showDefaultError();
        }
    }

    private void showDefaultError() {
        ((ActivityAware) context).getCurrentActivity().showToast("Page Not Found");
    }

    private void launchProductList(DestinationInfo destinationInfo) {
        ArrayList<NameValuePair> nameValuePairs = destinationInfo.getProductQueryParams();
        if (nameValuePairs != null && nameValuePairs.size() > 0) {
            Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), ProductListActivity.class);
            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_GENERIC_PRODUCT_LIST);
            intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
            String title = sectionItem.getTitle() != null ? sectionItem.getTitle().getText() : null;
            if (!TextUtils.isEmpty(title)) {
                intent.putExtra(Constants.TITLE, title);
            }
            ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } else {
            showDefaultError();
        }
    }

    private void logAnalytics() {
        if (section == null || sectionItem == null || TextUtils.isEmpty(section.getSectionType())) return;
        // nc
        // section.getSectionType() + (section.getTitle() != null ? section.getTitle().getText() : "")

        // item
        // (sectionItem.getTitle() != null ? sectionItem.getTile().getText() : "")
    }

    private void logMainMenuEvent(String trackAwareName, String eventKeyName,
                                  String navigationCtx){
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(eventKeyName, navigationCtx);
        ((TrackingAware)context).trackEvent(trackAwareName, eventAttribs);
    }
}