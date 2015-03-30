package com.bigbasket.mobileapp.handler;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.fragment.product.SubCategoryListFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoCategoryFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoDetailFragment;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.SectionManager;
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
        /*
          if (section.getSectionType() != null &&
                section.getSectionType().equalsIgnoreCase(Section.PRODUCT_CAROUSEL)) {
            DestinationInfo destinationInfo = sectionItem.getDestinationInfo();
            if (destinationInfo != null) {
                String destinationType = destinationInfo.getDestinationType();
                String destinationSlug = destinationInfo.getDestinationSlug();
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                        getApiService(((ActivityAware) context).getCurrentActivity());
                if (!TextUtils.isEmpty(destinationSlug) && !TextUtils.isEmpty(destinationType) &&
                        destinationType.equals(DestinationInfo.SHOPPING_LIST)) {
                    if (!((ConnectivityAware) context).checkInternetConnection()) {
                        ((HandlerAware) context).getHandler().sendOfflineError();
                        return;
                    }
                    ((ProgressIndicationAware) context).showProgressDialog("Please wait...");
                    bigBasketApiService.getShoppingListDetails(destinationSlug, null, new Callback<ApiResponse<GetShoppingListDetailsApiResponse>>() {
                        @Override
                        public void success(ApiResponse<GetShoppingListDetailsApiResponse> getShoppingListDetailsApiResponse, Response response) {
                            if (((CancelableAware) context).isSuspended()) return;
                            try {
                                ((ProgressIndicationAware) context).hideProgressDialog();
                            } catch (IllegalArgumentException e) {
                                return;
                            }
                            switch (getShoppingListDetailsApiResponse.status) {
                                case 0:
                                    ShoppingListDetail shoppingListDetail = getShoppingListDetailsApiResponse.apiResponseContent.shoppingListDetail;
                                    if (shoppingListDetail != null) {
                                        ArrayList<Product> products = shoppingListDetail.getProducts();
                                        String title = section.getTitle() != null ? section.getTitle().getText() : null;
                                        ((ProductListDialogAware) context).showDialog(title, products, products.size(), getShoppingListDetailsApiResponse.apiResponseContent.baseImgUrl,
                                                true, Constants.SHOPPING_LISTS);
                                    }
                                    break;
                                default:
                                    ((HandlerAware) context).getHandler().
                                            sendEmptyMessage(getShoppingListDetailsApiResponse.status,
                                                    getShoppingListDetailsApiResponse.message);
                                    break;
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (((CancelableAware) context).isSuspended()) return;
                            try {
                                ((ProgressIndicationAware) context).hideProgressDialog();
                            } catch (IllegalArgumentException e) {
                                return;
                            }
                            ((HandlerAware) context).getHandler().handleRetrofitError(error);
                        }
                    });
                } else if (!TextUtils.isEmpty(destinationType)) {
                    launchProductList() // Add count
                }
            }
        }

        */
//      if (section.getSectionType() != null &&
//              section.getSectionType().equalsIgnoreCase(Section.PRODUCT_CAROUSEL)) {
//          DestinationInfo destinationInfo = sectionItem.getDestinationInfo();
//          if (destinationInfo != null) {
//              launchProductList(destinationInfo);
//          }else {
//              // handle this case
//          }
//      }else
        if (sectionItem.getDestinationInfo() != null &&
                sectionItem.getDestinationInfo().getDestinationType() != null) {
            DestinationInfo destinationInfo = sectionItem.getDestinationInfo();
            switch (destinationInfo.getDestinationType()) {
                case DestinationInfo.CATEGORY_LANDING:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        String title = sectionItem.getTitle() != null ? sectionItem.getTitle().getText() : "";
                        if (hasMainMenu()) {
                            SubCategoryListFragment subCategoryListFragment = new SubCategoryListFragment();
                            Bundle subCatBundle = new Bundle();
                            subCatBundle.putString(Constants.TOP_CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                            subCatBundle.putString(Constants.TOP_CATEGORY_NAME, title);
                            subCategoryListFragment.setArguments(subCatBundle);
                            ((BBActivity) context).onChangeFragment(subCategoryListFragment);
                        } else {
                            Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), BBActivity.class);
                            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CATEGORY_LANDING);
                            intent.putExtra(Constants.TOP_CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                            intent.putExtra(Constants.TOP_CATEGORY_NAME, title);
                            ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }
                    }
//                    else {
//                        showDefaultError();
//                    }
                    break;
                case DestinationInfo.FLAT_PAGE:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), FlatPageWebViewActivity.class);
                        intent.putExtra(Constants.WEBVIEW_URL, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
//                    else {
//                        showDefaultError();
//                    }
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
                        intent.putExtra(Constants.CATEGORY_TITLE, sectionItem.getTitle() != null ? sectionItem.getTitle().getText() :
                                section.getTitle() != null ? section.getTitle().getText() : "");
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
//                    else {
//                        showDefaultError();
//                    }
                    break;
                case DestinationInfo.PRODUCT_DETAIL:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                        intent.putExtra(Constants.SKU_ID, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
//                    else {
//                        showDefaultError();
//                    }
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
//                    else {
//                        showDefaultError();
//                    }
                    break;
                case DestinationInfo.SHOPPING_LIST_SUMMARY:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        boolean isSmartBasket = destinationInfo.getDestinationSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG);
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), ShoppingListSummaryActivity.class);
                        ShoppingListName shoppingListName = new ShoppingListName(destinationInfo.getDestinationSlug(), destinationInfo.getDestinationSlug(),
                                isSmartBasket);
                        intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);

                        if (isSmartBasket) logMainMenuEvent(TrackingAware.SMART_BASKET_ICON_CLICKED,
                                TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_HOME_PAGE);
                        else logMainMenuEvent(TrackingAware.SHOPPING_LIST_ICON_CLICKED,
                                TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_HOME_PAGE);
                    }
//                    else {
//                        showDefaultError();
//                    }
                    break;
                case DestinationInfo.SHOPPING_LIST_LANDING:
                    intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    logMainMenuEvent(TrackingAware.SHOPPING_LIST_ICON_CLICKED, TrackEventkeys.NAVIGATION_CTX,
                            TrackEventkeys.NAVIGATION_CTX_HOME_PAGE);
                    break;
                case DestinationInfo.SEARCH:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), ProductListActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SEARCH);
                        intent.putExtra(Constants.SEARCH_QUERY, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
//                    else {
//                        showDefaultError();
//                    }
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
//                    else {
//                        showDefaultError();
//                    }
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
                case DestinationInfo.SHOPPING_LIST:
                    intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
//                default:
//                    showDefaultError();
//                    break;
            }
        }
//        else {
//            showDefaultError();
//        }
    }
//
//    private void showDefaultError() {
//        ((ActivityAware) context).getCurrentActivity().showToast("Page Not Found");
//    }

    private void launchProductList(DestinationInfo destinationInfo) {
        ArrayList<NameValuePair> nameValuePairs = destinationInfo.getProductQueryParams();
        if (nameValuePairs != null && nameValuePairs.size() > 0) {
            Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), ProductListActivity.class);
            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_GENERIC_PRODUCT_LIST);
            intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
            if (!TextUtils.isEmpty(getSectionName()) || !TextUtils.isEmpty(getSectionItemName()))
                intent.putExtra(TrackEventkeys.NAVIGATION_CTX, getSectionName() + "." + getSectionItemName());//todo Check with sid
            String title = sectionItem.getTitle() != null ? sectionItem.getTitle().getText() : null;
            if (!TextUtils.isEmpty(title)) {
                intent.putExtra(Constants.TITLE, title);
            }
            ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
//        else {
//            showDefaultError();
//        }
    }

    private void logClickEvent() {
        if (section == null) return;
        if (section.getSectionType().equals(Section.BANNER)) {
            logBannerEvent();
        } else if (screenName != null) {
            if (screenName.equals(SectionManager.HOME_PAGE)) {
                logItemClickEvent(TrackingAware.HOME_PAGE_ITEM_CLICKED);
            } else if (screenName.equals(SectionManager.MAIN_MENU)) {
                logItemClickEvent(TrackingAware.MENU_ITEM_CLICKED);
            }
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
        ((TrackingAware) context).trackEvent(TrackingAware.HOME_PAGE_BANNER_CLICKED, eventAttribs);
    }

    private String getSectionItemName() {
        if (sectionItem == null || TextUtils.isEmpty(section.getSectionType()))
            return "";
        return sectionItem.getTitle() != null ? sectionItem.getTitle().getText() :
                sectionItem.getDescription() != null ?
                        !TextUtils.isEmpty(sectionItem.getDescription().getText()) ?
                                sectionItem.getDescription().getText() : "" : "";
    }

    private void logItemClickEvent(String trackAwareName) {
        HashMap<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.SECTION_TYPE, getSectionName());
        eventAttribs.put(TrackEventkeys.SECTION_ITEM, getSectionItemName());
        ((TrackingAware) context).trackEvent(trackAwareName, eventAttribs);
    }

    private void logMainMenuEvent(String trackAwareName, String eventKeyName,
                                  String navigationCtx) {
        HashMap<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(eventKeyName, navigationCtx);
        ((TrackingAware) context).trackEvent(trackAwareName, eventAttribs);
    }

    private boolean hasMainMenu() {
        return context instanceof BBActivity && !(context instanceof BackButtonActivity);
    }
}