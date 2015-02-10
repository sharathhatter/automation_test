package com.bigbasket.mobileapp.handler;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.ProductListApiResponseCallback;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListDetailsApiResponse;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ProductListDialogAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.model.product.ProductQuery;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListDetail;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OnSectionItemClickListener<T> implements View.OnClickListener, BaseSliderView.OnSliderClickListener,
        ProductListDataAware {
    private T context;
    private Section section;
    private SectionItem sectionItem;
    private ProductListData productListData;

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
                    ProductQuery productQuery = ProductQuery.convertDestinationTypeToProductQuery(destinationType, destinationSlug);
                    if (productQuery != null) {
                        if (!((ConnectivityAware) context).checkInternetConnection()) {
                            ((HandlerAware) context).getHandler().sendOfflineError();
                            return;
                        }
                        ((ProgressIndicationAware) context).showProgressDialog("Please wait...");
                        bigBasketApiService.productListUrl(productQuery.getAsQueryMap(), new ProductListApiResponseCallback<>(1, this, false));
                    }
                }
            }
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
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), ProductListActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SEARCH);
                        intent.putExtra(Constants.SEARCH_QUERY, destinationInfo.getDestinationSlug());
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                    break;
                case DestinationInfo.PRODUCT_LIST:
                    ProductQuery productQuery = ProductQuery.convertDestinationTypeToProductQuery(
                            destinationInfo.getDestinationType(), destinationInfo.getDestinationSlug());
                    if (productQuery != null) {
                        intent = new Intent(((ActivityAware) context).getCurrentActivity(), ProductListActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_GENERIC_PRODUCT_LIST);
                        intent.putExtra(Constants.TYPE, productQuery.getType());
                        intent.putExtra(Constants.SLUG, productQuery.getSlug());
                        String title = sectionItem.getTitle() != null ? sectionItem.getTitle().getText() : null;
                        if (!TextUtils.isEmpty(title)) {
                            intent.putExtra(Constants.TITLE, title);
                        }
                        ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
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
                    intent = new Intent(((ActivityAware) context).getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_CATEGORY);
                    ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
            }
        }
    }

    @Override
    public ProductListData getProductListData() {
        return productListData;
    }

    @Override
    public void setProductListData(ProductListData productListData) {
        this.productListData = productListData;
    }

    @Override
    public void updateData() {
        String title = section.getTitle() != null ? section.getTitle().getText() : null;
        ((ProductListDialogAware) context).showDialog(title,
                productListData.getProducts(), productListData.getProductCount(),
                productListData.getBaseImgUrl(), true, DestinationInfo.PRODUCT_LIST);
    }

    @Override
    public void updateProductList(List<Product> nextPageProducts) {

    }

    @Override
    public boolean isNextPageLoading() {
        return false;
    }

    @Override
    public void setNextPageLoading(boolean isNextPageLoading) {

    }

    @Override
    public ProductQuery getProductQuery() {
        return null;
    }
}