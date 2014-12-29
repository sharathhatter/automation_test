package com.bigbasket.mobileapp.apiservice.callbacks;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.util.ApiErrorCodes;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProductListApiResponseCallback<T> implements Callback<ApiResponse<ProductListData>> {

    private int page;
    private T ctx;

    public ProductListApiResponseCallback(int page, T ctx) {
        this.page = page;
        this.ctx = ctx;
    }

    @Override
    public void success(ApiResponse<ProductListData> productListDataApiResponse, Response response) {
        if (((CancelableAware) ctx).isSuspended()) return;
        if (page == 1) {
            try {
                ((ProgressIndicationAware) ctx).hideProgressView();
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        if (productListDataApiResponse.status == 0) {
            ProductListData productListData = productListDataApiResponse.apiResponseContent;
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
                if (filteredOns != null && filteredOns.size() > 0) {
                    for (FilterOptionCategory filterOptionCategory : filterOptionCategories) {
                        for (FilterOptionItem filterOptionItem : filterOptionCategory.getFilterOptionItems()) {
                            for (FilteredOn filteredOn : filteredOns) {
                                if (filteredOn.getFilterSlug().equalsIgnoreCase(filterOptionItem.getFilterValueSlug())
                                        && filteredOn.getFilterValues() != null
                                        && filteredOn.getFilterValues().size() > 0) {
                                    filterOptionItem.setSelected(true);
                                }
                            }
                        }
                    }
                }
                ((ProductListDataAware) ctx).setProductListData(productListData);
                ((ProductListDataAware) ctx).updateData();
            }
        } else {
            ((HandlerAware) ctx).getHandler().sendEmptyMessage(ApiErrorCodes.SERVER_ERROR);
        }
    }

    @Override
    public void failure(RetrofitError error) {
        if (((CancelableAware) ctx).isSuspended()) return;
        if (page == 1) {
            try {
                ((ProgressIndicationAware) ctx).hideProgressView();
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        ((HandlerAware) ctx).getHandler().sendEmptyMessage(ApiErrorCodes.SERVER_ERROR);
    }
}
