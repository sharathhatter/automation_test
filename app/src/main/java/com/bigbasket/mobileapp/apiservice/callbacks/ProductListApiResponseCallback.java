package com.bigbasket.mobileapp.apiservice.callbacks;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ProductListApiResponse;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.SectionAware;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.ProductListData;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProductListApiResponseCallback<T> implements Callback<ApiResponse<ProductListApiResponse>> {

    private int page;
    private T ctx;
    private boolean isInlineProgressBar;

    public ProductListApiResponseCallback(int page, T ctx) {
        this.page = page;
        this.ctx = ctx;
    }

    public ProductListApiResponseCallback(int page, T ctx, boolean isInlineProgressBar) {
        this(page, ctx);
        this.isInlineProgressBar = isInlineProgressBar;
    }

    @Override
    public void success(ApiResponse<ProductListApiResponse> productListDataApiResponse, Response response) {
        if (((CancelableAware) ctx).isSuspended()) return;
        if (page == 1) {
            try {
                if (isInlineProgressBar) {
                    ((ProgressIndicationAware) ctx).hideProgressView();
                } else {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                }
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        if (productListDataApiResponse.status == 0) {
            ProductListData productListData = productListDataApiResponse.apiResponseContent.productListData;
            if (page > 1) {
                ProductListData existingProductListData = ((ProductListDataAware) ctx).getProductListData();
                existingProductListData.setCurrentPage(productListData.getCurrentPage());
                ((ProductListDataAware) ctx).updateProductList(productListData.getProducts());
            } else {
                if (productListData.getFilteredOn() == null) {
                    productListData.setFilteredOn(new ArrayList<FilteredOn>());
                }

                ArrayList<FilterOptionCategory> filterOptionCategories = productListData.getFilterOptions();
                ArrayList<FilteredOn> filteredOns = productListData.getFilteredOn();
                if (filteredOns != null && filteredOns.size() > 0 && filterOptionCategories != null) {
                    for (FilterOptionCategory filterOptionCategory : filterOptionCategories) {
                        for (FilteredOn filteredOn : filteredOns) {
                            if (filteredOn.getFilterSlug() != null &&
                                    filteredOn.getFilterSlug().equals(filterOptionCategory.getFilterSlug())
                                    && filterOptionCategory.getFilterOptionItems() != null) {
                                for (FilterOptionItem filterOptionItem : filterOptionCategory.getFilterOptionItems()) {
                                    if (filterOptionItem.getFilterValueSlug() != null &&
                                            filteredOn.getFilterValues() != null &&
                                            filteredOn.getFilterValues().contains(filterOptionItem.getFilterValueSlug())) {
                                        filterOptionItem.setSelected(true);
                                    }
                                }
                            }
                        }
                    }
                }
                ((ProductListDataAware) ctx).setProductListData(productListData);
                if (ctx instanceof SectionAware) {
                    ((SectionAware) ctx).setSectionData(productListDataApiResponse.apiResponseContent.sectionData);
                }
                ((ProductListDataAware) ctx).updateData();
            }
        } else {
            ((HandlerAware) ctx).getHandler().sendEmptyMessage(productListDataApiResponse.status,
                    productListDataApiResponse.message, true);
        }
    }

    @Override
    public void failure(RetrofitError error) {
        if (((CancelableAware) ctx).isSuspended()) return;
        if (page == 1) {
            try {
                if (isInlineProgressBar) {
                    ((ProgressIndicationAware) ctx).hideProgressView();
                } else {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                }
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        ((HandlerAware) ctx).getHandler().handleRetrofitError(error, true);
    }
}
