package com.bigbasket.mobileapp.handler;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.activity.product.DiscountActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.fragment.DynamicScreenFragment;
import com.bigbasket.mobileapp.fragment.product.CategoryLandingFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoCategoryFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoDetailFragment;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.AnalyticsNavigationContextAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.SectionManager;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;

import java.util.ArrayList;
import java.util.HashMap;

public class OnSectionItemClickListener<T> implements View.OnClickListener, BaseSliderView.OnSliderClickListener {
    private T context;
    private Section section;
    private SectionItem sectionItem;
    private String screenName;

    public OnSectionItemClickListener(T context, Section section, SectionItem sectionItem,
                                      String screenName) {
        this.context = context;
        this.section = section;
        this.sectionItem = sectionItem;
        this.screenName = screenName;
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

        logClickEvent();
        if (sectionItem.getDestinationInfo() != null &&
                sectionItem.getDestinationInfo().getDestinationType() != null) {
            DestinationInfo destinationInfo = sectionItem.getDestinationInfo();
            switch (destinationInfo.getDestinationType()) {
                case DestinationInfo.CATEGORY_LANDING:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        String title = sectionItem.getTitle() != null ? sectionItem.getTitle().getText() : "";
                        if (hasMainMenu()) {
                            CategoryLandingFragment categoryLandingFragment = new CategoryLandingFragment();
                            Bundle subCatBundle = new Bundle();
                            subCatBundle.putString(Constants.TOP_CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                            subCatBundle.putString(Constants.TOP_CATEGORY_NAME, title);
                            subCatBundle.putString(Constants.TOP_CATEGORY_VERSION, destinationInfo.getCacheVersion());
                            categoryLandingFragment.setArguments(subCatBundle);
                            ((BBActivity) context).onChangeFragment(categoryLandingFragment);
                        } else {
                            Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), BBActivity.class);
                            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CATEGORY_LANDING);
                            intent.putExtra(Constants.TOP_CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                            intent.putExtra(Constants.TOP_CATEGORY_NAME, title);
                            intent.putExtra(Constants.TOP_CATEGORY_VERSION, destinationInfo.getCacheVersion());
                            ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }
                    }
                    break;
                case DestinationInfo.FLAT_PAGE:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), FlatPageWebViewActivity.class);
                        intent.putExtra(Constants.WEBVIEW_URL, destinationInfo.getDestinationSlug());
                        intent.putExtra(Constants.WEBVIEW_TITLE, sectionItem.getTitle() != null ?
                                sectionItem.getTitle().getText() : null);
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
                case DestinationInfo.PREVIOUS_ORDERS:
                    Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), OrderListActivity.class);
                    intent.putExtra(Constants.ORDER, ((ActivityAware) context).getCurrentActivity().getString(R.string.past_label));
                    intent.putExtra(Constants.SHOP_FROM_PREVIOUS_ORDER, true);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case DestinationInfo.PRODUCT_DETAIL:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonWithBasketButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                        intent.putExtra(Constants.SKU_ID, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
                case DestinationInfo.PROMO_DETAIL:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())
                            && TextUtils.isDigitsOnly(destinationInfo.getDestinationSlug())) {
                        if (hasMainMenu()) {
                            Bundle promoDetailBundle = new Bundle();
                            promoDetailBundle.putInt(Constants.PROMO_ID, Integer.parseInt(destinationInfo.getDestinationSlug()));
                            PromoDetailFragment promoDetailFragment = new PromoDetailFragment();
                            promoDetailFragment.setArguments(promoDetailBundle);
                            ((BBActivity) context).onChangeFragment(promoDetailFragment);
                        } else {
                            intent = new Intent(((ActivityAware) context).getCurrentActivity(), BBActivity.class);
                            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
                            intent.putExtra(Constants.PROMO_ID, Integer.parseInt(destinationInfo.getDestinationSlug()));
                            ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }
                    }
                    break;
                case DestinationInfo.SHOPPING_LIST_SUMMARY:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        boolean isSmartBasket = destinationInfo.getDestinationSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG);
                        String title;
                        if (isSmartBasket) {
                            title = Constants.SMART_BASKET;
                        } else {
                            title = section.getTitle() != null ? section.getTitle().getText() : "";
                        }
                        ShoppingListName shoppingListName = new ShoppingListName(title, destinationInfo.getDestinationSlug(),
                                isSmartBasket);
                        ((LaunchProductListAware) context).launchShoppingList(shoppingListName);
                    }
                    break;
                case DestinationInfo.SHOPPING_LIST_LANDING:
                    intent = new Intent(((ActivityAware) context).getCurrentActivity(), ShoppingListActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case DestinationInfo.SEARCH:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH.get()));
                        nameValuePairs.add(new NameValuePair(Constants.SLUG, destinationInfo.getDestinationSlug().trim()));
                        launchProductList(nameValuePairs);
                    }
                    break;
                case DestinationInfo.PRODUCT_CATEGORY:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.CATEGORY.get()));
                        nameValuePairs.add(new NameValuePair(Constants.SLUG, destinationInfo.getDestinationSlug()));
                        launchProductList(nameValuePairs);
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
                    }
                    break;
                case DestinationInfo.PROMO_LIST:
                    if (hasMainMenu()) {
                        ((BBActivity) context).onChangeFragment(new PromoCategoryFragment());
                    } else {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BBActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_CATEGORY);
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
                case DestinationInfo.HOME:
                    ((ActivityAware) context).getCurrentActivity().goToHome(false);
                    break;
                case DestinationInfo.COMMUNICATION_HUB:
                    ((ActivityAware) context).getCurrentActivity().launchMoEngageCommunicationHub();
                    break;
                case DestinationInfo.CALL:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        try {
                            String uri = "tel:" + destinationInfo.getDestinationSlug();
                            intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse(uri));
                            ((ActivityAware) context).getCurrentActivity().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            // Do nothing
                        }
                    }
                    break;
                case DestinationInfo.DYNAMIC_PAGE:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.SCREEN, destinationInfo.getDestinationSlug());
                        DynamicScreenFragment dynamicScreenFragment = new DynamicScreenFragment();
                        dynamicScreenFragment.setArguments(bundle);
                        ((ActivityAware) context).getCurrentActivity().onChangeFragment(dynamicScreenFragment);
                    }
                    break;
                case DestinationInfo.DISCOUNT:
                    intent = new Intent(((ActivityAware) context).getCurrentActivity(),
                            DiscountActivity.class);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent,
                            NavigationCodes.GO_TO_HOME);
                    break;
                case DestinationInfo.BASKET:
                    ((ActivityAware) context).getCurrentActivity().launchViewBasketScreen();
                    break;
            }
        }
    }

    private void launchProductList(DestinationInfo destinationInfo) {
        ArrayList<NameValuePair> nameValuePairs = UIUtil.getProductQueryParams(destinationInfo.getDestinationSlug());
        launchProductList(nameValuePairs);
    }

    private void launchProductList(ArrayList<NameValuePair> nameValuePairs) {
        ((LaunchProductListAware) context).launchProductList(nameValuePairs, getSectionName(), getSectionItemName());
    }

    private void logClickEvent() {
        if (section == null) return;
        if (section.getSectionType().equals(Section.BANNER)) {
            logBannerEvent();
        } else if (screenName != null) {
            logItemClickEvent();
        }
        setNc();
    }

    private void setNc() {
        StringBuilder ncBuilder = new StringBuilder();
        if (screenName != null) {
            switch (screenName) {
                case SectionManager.HOME_PAGE:
                    ncBuilder.append(TrackEventkeys.HOME);
                    break;
                case SectionManager.MAIN_MENU:
                    ncBuilder.append(TrackEventkeys.MENU);
                    break;
                default:
                    ncBuilder.append(TrackEventkeys.SCREEN);
                    if (!TextUtils.isEmpty(screenName)) {
                        ncBuilder.append(".").append(screenName);
                    }
                    break;
            }
        } else {
            ncBuilder.append(TrackEventkeys.SCREEN);
        }
        if (section != null) {
            if (section.getTitle() != null &&
                    !TextUtils.isEmpty(section.getTitle().getText())) {
                ncBuilder.append(".").append(section.getTitle().getText());
            } else if (section.getDescription() != null && !TextUtils.isEmpty(section.getDescription().getText())) {
                ncBuilder.append(".").append(section.getDescription().getText());
            }
        }
        if (sectionItem != null) {
            if (sectionItem.getTitle() != null &&
                    !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                ncBuilder.append(".").append(sectionItem.getTitle().getText());
            } else if (sectionItem.getDescription() != null && !TextUtils.isEmpty(sectionItem.getDescription().getText())) {
                ncBuilder.append(".").append(sectionItem.getDescription().getText());
            }
        }
        if (context instanceof Fragment && context instanceof AnalyticsNavigationContextAware) {
            ((AnalyticsNavigationContextAware) context).setNextScreenNavigationContext(ncBuilder.toString());
        }
        ((ActivityAware) context).getCurrentActivity().setNextScreenNavigationContext(ncBuilder.toString());
    }

    private String getAnalyticsFormattedScreeName() {
        switch (screenName) {
            case SectionManager.HOME_PAGE:
                return TrackingAware.HOME_PAGE_ITEM_CLICKED;
            case SectionManager.MAIN_MENU:
                return TrackingAware.MENU_ITEM_CLICKED;
            default:
                return screenName + "." + TrackingAware.ITEM_CLICKED;
        }
    }

    private String getSectionName() {
        if (section == null || TextUtils.isEmpty(section.getSectionType()))
            return "";

        return section.getTitle() != null ? section.getTitle().getText() : section.getSectionType();
    }

    private void logBannerEvent() {
        if (sectionItem == null || sectionItem.getDestinationInfo() != null) return;
        int index = 0;
        for (int i = 0; i < section.getSectionItems().size(); i++) {
            if (section.getSectionItems().get(i) == sectionItem)
                index = i;
        }

        String bannerName = "";
        if (!TextUtils.isEmpty(sectionItem.getDestinationInfo().getDestinationType())) {
            bannerName = sectionItem.getDestinationInfo().getDestinationType();
        }
        if (!TextUtils.isEmpty(sectionItem.getDestinationInfo().getDestinationSlug())) {
            bannerName += ", " + sectionItem.getDestinationInfo().getDestinationSlug();
        }

        HashMap<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.BANNER_ID, String.valueOf(index));
        eventAttribs.put(TrackEventkeys.BANNER_SLUG, bannerName);
        ((TrackingAware) context).trackEvent(getAnalyticsFormattedScreeName(), eventAttribs);
    }

    private String getSectionItemName() {
        if (sectionItem == null || TextUtils.isEmpty(section.getSectionType()))
            return "";
        return sectionItem.getTitle() != null ? sectionItem.getTitle().getText() :
                sectionItem.getDescription() != null ?
                        !TextUtils.isEmpty(sectionItem.getDescription().getText()) ?
                                sectionItem.getDescription().getText() : "" : "";
    }

    private void logItemClickEvent() {
        HashMap<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.SECTION_TYPE, getSectionName());
        String itemName = getSectionItemName();
        if (!TextUtils.isEmpty(itemName))
            eventAttribs.put(TrackEventkeys.SECTION_ITEM, getSectionItemName());
        ((TrackingAware) context).trackEvent(getAnalyticsFormattedScreeName(), eventAttribs);
    }

    private boolean hasMainMenu() {
        return context instanceof BBActivity && !(context instanceof BackButtonActivity);
    }
}