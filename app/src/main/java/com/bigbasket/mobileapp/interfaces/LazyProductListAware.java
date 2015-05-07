package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.product.Product;

import java.util.ArrayList;

public interface LazyProductListAware {
    @Nullable
    ArrayList<Product> provideProductsIfAvailable(String tabType);
}
