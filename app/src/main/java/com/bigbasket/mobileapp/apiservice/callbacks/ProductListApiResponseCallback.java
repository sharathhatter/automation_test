package com.bigbasket.mobileapp.apiservice.callbacks;

import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.ProductTabData;
import com.bigbasket.mobileapp.model.product.ProductTabInfo;

import java.util.ArrayList;

public class ProductListApiResponseCallback<T extends AppOperationAware> extends BBNetworkCallback<ApiResponse<ProductTabData>> {

    private T ctx;
    private boolean isInlineProgressBar;
    private boolean isFilterOrSortApplied;

    public ProductListApiResponseCallback(T ctx, boolean isInlineProgressBar,
                                          boolean isFilterOrSortApplied) {
        super(ctx);
        this.ctx = ctx;
        this.isInlineProgressBar = isInlineProgressBar;
        this.isFilterOrSortApplied = isFilterOrSortApplied;
    }

    @Override
    public void onSuccess(ApiResponse<ProductTabData> productListDataApiResponse) {
        if (productListDataApiResponse.status == 0) {
            ProductTabData productTabData = productListDataApiResponse.apiResponseContent;
            if (productTabData != null && productTabData.getProductTabInfos() != null) {
                for (ProductTabInfo productTabInfo : productTabData.getProductTabInfos()) {
                    if (productTabInfo.getFilteredOn() == null) {
                        productTabInfo.setFilteredOn(new ArrayList<FilteredOn>());
                    }
                }

                for (ProductTabInfo productTabInfo : productTabData.getProductTabInfos()) {
                    ArrayList<FilterOptionCategory> filterOptionCategories = productTabInfo.getFilterOptionItems();
                    ArrayList<FilteredOn> filteredOns = productTabInfo.getFilteredOn();
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
            }
            ((ProductListDataAware) ctx).setProductTabData(productTabData,
                    isFilterOrSortApplied);

        } else {
            ctx.getHandler().sendEmptyMessage(productListDataApiResponse.status,
                    productListDataApiResponse.message, true);
        }
    }

    @Override
    public boolean updateProgress() {
        try {
            if (isInlineProgressBar) {
                ctx.hideProgressView();
            } else {
                ctx.hideProgressDialog();
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
