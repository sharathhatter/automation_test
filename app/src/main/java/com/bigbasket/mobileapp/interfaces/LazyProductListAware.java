package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.bigbasket.mobileapp.model.product.Product;

import java.util.ArrayList;

public interface LazyProductListAware {
    @Nullable
    Pair<ArrayList<Product>, Integer> provideProductsIfAvailable(String tabType);
}
