package com.bigbasket.mobileapp.apiservice.callbacks;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.ProductTabData;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProductListApiResponseCallback<T> implements Callback<ApiResponse<ProductTabData>> {

    private T ctx;
    private boolean isInlineProgressBar;

    public ProductListApiResponseCallback(T ctx, boolean isInlineProgressBar) {
        this.ctx = ctx;
        this.isInlineProgressBar = isInlineProgressBar;
    }

    @Override
    public void success(ApiResponse<ProductTabData> productListDataApiResponse, Response response) {
        if (((CancelableAware) ctx).isSuspended()) return;
        try {
            if (isInlineProgressBar) {
                ((ProgressIndicationAware) ctx).hideProgressView();
            } else {
                ((ProgressIndicationAware) ctx).hideProgressDialog();
            }
        } catch (IllegalArgumentException e) {
            return;
        }
        if (productListDataApiResponse.status == 0) {
            ProductTabData productTabData = productListDataApiResponse.apiResponseContent;
            if (productTabData != null) {
                if (productTabData.getFilteredOn() == null) {
                    productTabData.setFilteredOn(new ArrayList<FilteredOn>());
                }

                ArrayList<FilterOptionCategory> filterOptionCategories = productTabData.getFilterOptionItems();
                ArrayList<FilteredOn> filteredOns = productTabData.getFilteredOn();
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
            }
            ((ProductListDataAware) ctx).setProductTabData(productTabData);

        } else {
            ((HandlerAware) ctx).getHandler().sendEmptyMessage(productListDataApiResponse.status,
                    productListDataApiResponse.message, true);
        }
    }

    @Override
    public void failure(RetrofitError error) {
        if (((CancelableAware) ctx).isSuspended()) return;
        try {
            if (isInlineProgressBar) {
                ((ProgressIndicationAware) ctx).hideProgressView();
            } else {
                ((ProgressIndicationAware) ctx).hideProgressDialog();
            }
        } catch (IllegalArgumentException e) {
            return;
        }
        ((HandlerAware) ctx).getHandler().handleRetrofitError(error, true);
    }
}
