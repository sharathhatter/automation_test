package com.bigbasket.mobileapp.handler.click;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.HomeActivity;
import com.bigbasket.mobileapp.activity.SectionHelpActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.SearchActivity;
import com.bigbasket.mobileapp.activity.product.DiscountActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListActivity;
import com.bigbasket.mobileapp.fragment.DynamicScreenFragment;
import com.bigbasket.mobileapp.fragment.product.CategoryLandingFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoCategoryFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoDetailFragment;
import com.bigbasket.mobileapp.interfaces.AnalyticsNavigationContextAware;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.interfaces.LaunchStoreListAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.managers.SectionHelpManager;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.HelpDestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.service.AbstractDynamicPageSyncService;
import com.bigbasket.mobileapp.slider.SliderTypes.BaseSliderView;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class OnSectionItemClickListener<T extends AppOperationAware> implements View.OnClickListener, BaseSliderView.OnSliderClickListener {
    protected T context;
    @Nullable
    protected Section section;
    @Nullable
    protected SectionItem sectionItem;
    @Nullable
    protected String screenName;

    public OnSectionItemClickListener(T context) {
        this.context = context;
    }

    public OnSectionItemClickListener(T context, @Nullable Section section,
                                      @Nullable SectionItem sectionItem,
                                      @Nullable String screenName) {
        this.context = context;
        this.section = section;
        this.sectionItem = sectionItem;
        this.screenName = screenName;
    }

    @Override
    public void onClick(View v) {
        Object obj = v.getTag(R.id.section_item_tag_id);
        if (obj instanceof SectionItem) {
            sectionItem = (SectionItem) obj;
        }
        onSectionClick();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Object obj = slider.getTag(R.id.section_item_tag_id);
        if (obj instanceof SectionItem) {
            sectionItem = (SectionItem) obj;
        }
        onSectionClick();
    }

    protected void onSectionClick() {
        if (context == null || context.isSuspended()) return;


        logClickEvent();
        if (sectionItem != null) {
            if (sectionItem.getHelpDestinationInfo() != null &&
                    sectionItem.getHelpDestinationInfo().getDestinationType() != null &&
                    !SectionHelpManager.isRead(context.getCurrentActivity(),
                            sectionItem.getHelpDestinationInfo().getHelpKey())) {
                HelpDestinationInfo helpDestinationInfo = sectionItem.getHelpDestinationInfo();
                SectionHelpManager.markAsRead(context.getCurrentActivity(),
                        helpDestinationInfo.getHelpKey());
                handleDestinationClick(helpDestinationInfo);
                return;
            }
            if (sectionItem.getDestinationInfo() != null &&
                    sectionItem.getDestinationInfo().getDestinationType() != null) {
                DestinationInfo destinationInfo = sectionItem.getDestinationInfo();
                handleDestinationClick(destinationInfo);
            }
        }
    }

    public void handleDestinationClick(DestinationInfo destinationInfo) {
        if (context == null || context.isSuspended()) return;
        switch (destinationInfo.getDestinationType()) {
            case DestinationInfo.CATEGORY_LANDING:
                if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                    if (sectionItem != null) {
                        String title = sectionItem.getTitle() != null ? sectionItem.getTitle().getText() : "";
                        if (hasMainMenu()) {
                            CategoryLandingFragment categoryLandingFragment = new CategoryLandingFragment();
                            Bundle subCatBundle = new Bundle();
                            subCatBundle.putString(Constants.TOP_CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                            subCatBundle.putString(Constants.TOP_CATEGORY_NAME, title);
                            subCatBundle.putString(Constants.TOP_CATEGORY_VERSION, destinationInfo.getCacheVersion());
                            categoryLandingFragment.setArguments(subCatBundle);
                            if (context instanceof BBActivity) {
                                ((BBActivity) context).onChangeFragment(categoryLandingFragment);
                            }
                        } else {
                            Intent intent = new Intent(context.getCurrentActivity(), SearchActivity.class);
                            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CATEGORY_LANDING);
                            intent.putExtra(Constants.TOP_CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                            intent.putExtra(Constants.TOP_CATEGORY_NAME, title);
                            intent.putExtra(Constants.TOP_CATEGORY_VERSION, destinationInfo.getCacheVersion());
                            context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }
                    }
                }
                break;
            case DestinationInfo.FLAT_PAGE:
                if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                    Intent intent;
                    if (destinationInfo instanceof HelpDestinationInfo) {
                        intent = new Intent(context.getCurrentActivity(), SectionHelpActivity.class);
                        intent.putExtra(Constants.SECTION_INFO, (Parcelable) section);
                        intent.putExtra(Constants.SECTION_ITEM, (Parcelable) sectionItem);
                    } else {
                        intent = new Intent(context.getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
                        intent.putExtra(Constants.WEBVIEW_URL, destinationInfo.getDestinationSlug());
                    }
                    if (sectionItem != null) {
                        intent.putExtra(Constants.WEBVIEW_TITLE, sectionItem.getTitle() != null ?
                                sectionItem.getTitle().getText() : null);
                    }
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
                break;
            case DestinationInfo.PREVIOUS_ORDERS:
                Intent intent = new Intent(context.getCurrentActivity(), OrderListActivity.class);
                intent.putExtra(Constants.ORDER, context.getCurrentActivity().getString(R.string.past_label));
                intent.putExtra(Constants.SHOP_FROM_PREVIOUS_ORDER, true);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case DestinationInfo.PRODUCT_DETAIL:
                if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                    intent = new Intent(context.getCurrentActivity(), BackButtonWithBasketButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                    intent.putExtra(Constants.SKU_ID, destinationInfo.getDestinationSlug());
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
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
                        intent = new Intent(context.getCurrentActivity(), SearchActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
                        intent.putExtra(Constants.PROMO_ID, Integer.parseInt(destinationInfo.getDestinationSlug()));
                        context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                }
                break;
            case DestinationInfo.SHOPPING_LIST_SUMMARY:
                if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                    boolean isSmartBasket = destinationInfo.getDestinationSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG);
                    String title = "";
                    if (isSmartBasket) {
                        title = Constants.SMART_BASKET;
                    } else {
                        if (section != null) {
                            title = section.getTitle() != null ? section.getTitle().getText() : "";
                        }
                    }
                    ShoppingListName shoppingListName = new ShoppingListName(title, destinationInfo.getDestinationSlug(),
                            isSmartBasket);
                    if (context instanceof LaunchProductListAware) {
                        ((LaunchProductListAware) context).launchShoppingList(shoppingListName);
                    }
                }
                break;
            case DestinationInfo.SHOPPING_LIST_LANDING:
                intent = new Intent(context.getCurrentActivity(), ShoppingListActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case DestinationInfo.SEARCH:
                if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                    nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH));
                    nameValuePairs.add(new NameValuePair(Constants.SLUG, destinationInfo.getDestinationSlug().trim()));
                    launchProductList(nameValuePairs);
                }
                break;
            case DestinationInfo.PRODUCT_CATEGORY:
                if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                    nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.CATEGORY));
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
                        context.getCurrentActivity().
                                startActivityForResult(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(destinationInfo.getDestinationSlug())),
                                        NavigationCodes.GO_TO_HOME);
                    } catch (ActivityNotFoundException e) {
                        // Do nothing
                    }
                }
                break;
            case DestinationInfo.PROMO_LIST:
                if (hasMainMenu() && context instanceof BBActivity) {
                    ((BBActivity) context).onChangeFragment(new PromoCategoryFragment());
                } else {
                    intent = new Intent(context.getCurrentActivity(), SearchActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_CATEGORY);
                    context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
                break;
            case DestinationInfo.HOME:
                context.getCurrentActivity().goToHome();
                break;
            case DestinationInfo.COMMUNICATION_HUB:
                context.getCurrentActivity().launchMoEngageCommunicationHub();
                break;
            case DestinationInfo.CALL:
                if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                    UIUtil.dialNumber(destinationInfo.getDestinationSlug(),
                            context.getCurrentActivity());
                }
                break;
            case DestinationInfo.DYNAMIC_PAGE:
                if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                    if (destinationInfo instanceof HelpDestinationInfo) {
                        intent = new Intent(context.getCurrentActivity(), SectionHelpActivity.class);
                        intent.putExtra(Constants.SECTION_INFO, (Parcelable) section);
                        intent.putExtra(Constants.SECTION_ITEM, (Parcelable) sectionItem);
                        context.getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.SCREEN, destinationInfo.getDestinationSlug());
                        DynamicScreenFragment dynamicScreenFragment = new DynamicScreenFragment();
                        dynamicScreenFragment.setArguments(bundle);
                        context.getCurrentActivity().onChangeFragment(dynamicScreenFragment);
                    }
                }
                break;
            case DestinationInfo.DISCOUNT:
                intent = new Intent(context.getCurrentActivity(),
                        DiscountActivity.class);
                context.getCurrentActivity().startActivityForResult(intent,
                        NavigationCodes.GO_TO_HOME);
                break;
            case DestinationInfo.BASKET:
                context.getCurrentActivity().launchViewBasketScreen();
                break;
            case DestinationInfo.STORE_LIST:
                launchStoreList(destinationInfo);
                break;
        }
    }

    private void launchStoreList(DestinationInfo destinationInfo) {
        if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())
                && context instanceof LaunchStoreListAware) {
            ((LaunchStoreListAware) context).launchStoreList(destinationInfo.getDestinationSlug());
        }
    }

    private void launchProductList(DestinationInfo destinationInfo) {
        ArrayList<NameValuePair> nameValuePairs = UIUtil.getProductQueryParams(
                destinationInfo.getDestinationSlug());
        launchProductList(nameValuePairs);
    }

    private void launchProductList(ArrayList<NameValuePair> nameValuePairs) {
        if (context instanceof LaunchProductListAware) {
            ((LaunchProductListAware) context).launchProductList(nameValuePairs,
                    getSectionName(), getSectionItemName(false));
        }
    }

    private void logClickEvent() {
        if (section == null) return;
        boolean isBannerClicked = section.getSectionType().equals(Section.BANNER);
        setNc(isBannerClicked);
        if (isBannerClicked) {
            logBannerEvent();
        } else if (screenName != null) {
            logItemClickEvent();
        }
    }

    /**
     * Override this function to return additional/custom navigation context
     */
    protected String getAdditionalNcValue() {
        return null;
    }

    private void setNc(boolean isBannerClicked) {
        StringBuilder ncBuilder = new StringBuilder();
        if (screenName != null) {
            switch (screenName) {
                case AbstractDynamicPageSyncService.HOME_PAGE:
                    ncBuilder.append(TrackEventkeys.HOME);
                    break;
                case AbstractDynamicPageSyncService.MAIN_MENU:
                    ncBuilder.append(TrackEventkeys.MENU);
                    break;
                case Constants.DISCOUNT_PAGE:
                    ncBuilder.append(Constants.DISCOUNT_PAGE);
                    break;
                default:
                    if (!TextUtils.isEmpty(screenName)) {
                        ncBuilder.append(screenName);
                    }
                    break;
            }
        } else {
            ncBuilder.append(TrackEventkeys.SCREEN);
        }
        if (isBannerClicked)
            ncBuilder.append(".").append(Section.BANNER);

        if (!TextUtils.isEmpty(getAdditionalNcValue())) {
            ncBuilder.append('.').append(getAdditionalNcValue());
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
                ncBuilder.append(".").append(sectionItem.getTitle().getText().replaceAll("\\(\\d+\\)", "").trim());
            } else if (sectionItem.hasImage() && !TextUtils.isEmpty(sectionItem.getImageName())) {
                ncBuilder.append(".").append(sectionItem.getImageName().replaceAll("[.]\\w+", ""));
            } else if (sectionItem.getDescription() != null && !TextUtils.isEmpty(sectionItem.getDescription().getText())) {
                ncBuilder.append(".").append(sectionItem.getDescription().getText());
            } else if (sectionItem.getDestinationInfo() != null &&
                    !TextUtils.isEmpty(sectionItem.getDestinationInfo().getDestinationSlug()) &&
                    sectionItem.getDestinationInfo().getDestinationSlug().contains(Constants.SLUG_PARAM)) {
                String typeAndSlug = sectionItem.getDestinationInfo().getDestinationSlug();
                int indexOfSlug = typeAndSlug.indexOf(Constants.SLUG_PARAM);
                String slug = typeAndSlug.substring(indexOfSlug + Constants.SLUG_PARAM.length());
                if (slug.contains("&")) {
                    int indexOfNextParam = slug.indexOf("&");
                    slug = slug.substring(0, indexOfNextParam);
                }
                if (!TextUtils.isEmpty(slug))
                    ncBuilder.append(".").append(slug);
            }
        }
        if (context instanceof Fragment && context instanceof AnalyticsNavigationContextAware) {
            ((AnalyticsNavigationContextAware) context).setNextScreenNavigationContext(ncBuilder.toString());
        }
        context.getCurrentActivity().setNextScreenNavigationContext(ncBuilder.toString());
    }

    @Nullable
    private String getAnalyticsFormattedScreeName() {
        if (screenName == null) return null;
        switch (screenName) {
            case AbstractDynamicPageSyncService.HOME_PAGE:
                return TrackingAware.HOME_PAGE_ITEM_CLICKED;
            case AbstractDynamicPageSyncService.MAIN_MENU:
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
        if (sectionItem == null || sectionItem.getDestinationInfo() == null) return;
        int index = 0;
        if (section != null) {
            for (int i = 0; i < section.getSectionItems().size(); i++) {
                if (section.getSectionItems().get(i) == sectionItem)
                    index = i;
            }
        }

        String bannerName = "";
        if (!TextUtils.isEmpty(sectionItem.getDestinationInfo().getDestinationType())) {
            bannerName = sectionItem.getDestinationInfo().getDestinationType();
        }
        if (!TextUtils.isEmpty(sectionItem.getDestinationInfo().getDestinationSlug())) {
            bannerName += ", " + sectionItem.getDestinationInfo().getDestinationSlug();
        }

        String eventName = getAnalyticsFormattedScreeName();
        if (eventName != null) {
            HashMap<String, String> eventAttribs = new HashMap<>();
            eventAttribs.put(TrackEventkeys.BANNER_ID, String.valueOf(index));
            eventAttribs.put(TrackEventkeys.BANNER_SLUG, bannerName);
            eventAttribs.put(TrackEventkeys.NAVIGATION_CTX,
                    context.getCurrentActivity().getNextScreenNavigationContext());
            if (context instanceof TrackingAware) {
                ((TrackingAware) context).trackEvent(eventName, eventAttribs);
            }
        }
    }

    private String getSectionItemName(boolean forAnalytics) {
        if (sectionItem == null)
            return "";
        if (sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
            return sectionItem.getTitle().getText();
        } else if (forAnalytics && sectionItem.hasImage() && !TextUtils.isEmpty(sectionItem.getImageName())) {
            return sectionItem.getImageName().replaceAll("[.]\\w+", "");
        } else if (sectionItem.getDescription() != null && !TextUtils.isEmpty(sectionItem.getDescription().getText())) {
            return sectionItem.getDescription().getText();
        } else if (forAnalytics && sectionItem.getDestinationInfo() != null &&
                !TextUtils.isEmpty(sectionItem.getDestinationInfo().getDestinationSlug()) &&
                sectionItem.getDestinationInfo().getDestinationSlug().contains(Constants.SLUG_PARAM)) {
            String typeAndSlug = sectionItem.getDestinationInfo().getDestinationSlug();
            int indexOfSlug = typeAndSlug.indexOf(Constants.SLUG_PARAM);
            String slug = typeAndSlug.substring(indexOfSlug + Constants.SLUG_PARAM.length());
            if (slug.contains("&")) {
                int indexOfNextParam = slug.indexOf("&");
                slug = slug.substring(0, indexOfNextParam);
            }
            if (!TextUtils.isEmpty(slug))
                return slug;
        }
        return "";
    }

    private void logItemClickEvent() {
        HashMap<String, String> eventAttribs = new HashMap<>();
        StringBuilder sectionItemNameBuilder = new StringBuilder();
        String sectionName = getSectionName();
        if (!TextUtils.isEmpty(sectionName)) {
            sectionItemNameBuilder.append(sectionName).append('.');
        }
        String itemName = getSectionItemName(true);
        if (!TextUtils.isEmpty(itemName)) {
            sectionItemNameBuilder.append(itemName);
        }
        if (!TextUtils.isEmpty(sectionItemNameBuilder)) {
            eventAttribs.put(TrackEventkeys.SECTION_ITEM, sectionItemNameBuilder.toString());
        }
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX,
                context.getCurrentActivity().getNextScreenNavigationContext());
        String eventName = getAnalyticsFormattedScreeName();
        if (eventName == null) return;
        if (context instanceof TrackingAware) {
            if (screenName != null && screenName.equals(Constants.DISCOUNT_PAGE)) {
                ((TrackingAware) context).trackEvent(eventName, eventAttribs,
                        null, null, false, true);
            } else {
                ((TrackingAware) context).trackEvent(eventName, eventAttribs);
            }
        }
    }

    private boolean hasMainMenu() {
        return context instanceof BBActivity && !(context instanceof BackButtonActivity)
                && !(context instanceof HomeActivity)
                && (((BaseActivity) context).findViewById(R.id.slidingTabs) == null)
                && ((BaseActivity) context).findViewById(R.id.content_frame) != null;
    }
}