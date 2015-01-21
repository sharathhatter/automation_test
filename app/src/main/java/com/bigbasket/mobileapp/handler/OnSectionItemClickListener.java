package com.bigbasket.mobileapp.handler;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;

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
        if (section.getSectionType() != null &&
                section.getSectionType().equalsIgnoreCase(Section.PRODUCT_CAROUSEL)) {

        } else if (sectionItem.getDestinationInfo() != null &&
                sectionItem.getDestinationInfo().getDestinationType() != null) {
            DestinationInfo destinationInfo = sectionItem.getDestinationInfo();
            switch (destinationInfo.getDestinationType()) {
                case DestinationInfo.CATEGORY_LANDING:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CATEGORY_LANDING);
                        intent.putExtra(Constants.CATEGORY_SLUG, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
                case DestinationInfo.FLAT_PAGE:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), FlatPageWebViewActivity.class);
                        intent.putExtra(Constants.WEBVIEW_URL, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
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
                    }
                    break;
                case DestinationInfo.PRODUCT_DETAIL:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
                        intent.putExtra(Constants.SKU_ID, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
                case DestinationInfo.PROMO_DETAIL:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())
                            && TextUtils.isDigitsOnly(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
                        intent.putExtra(Constants.PROMO_ID, Integer.parseInt(destinationInfo.getDestinationSlug()));
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
                case DestinationInfo.SHOPPING_LIST_SUMMARY:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        ShoppingListName shoppingListName = new ShoppingListName(destinationInfo.getDestinationSlug(), destinationInfo.getDestinationSlug(),
                                destinationInfo.getDestinationSlug().equalsIgnoreCase(Constants.SMART_BASKET_SLUG));
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_SUMMARY);
                        intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
                case DestinationInfo.SHOPPING_LIST_LANDING:
                    intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case DestinationInfo.SEARCH:
                    if (!TextUtils.isEmpty(destinationInfo.getDestinationSlug())) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SEARCH);
                        intent.putExtra(Constants.SEARCH_QUERY, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
            }
        }
    }
}