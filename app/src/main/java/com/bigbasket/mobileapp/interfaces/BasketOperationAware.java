package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.product.Product;

import java.util.HashMap;

public interface BasketOperationAware {
    void setBasketOperationResponse(BasketOperationResponse basketOperationResponse);

    void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView, View viewDecQty,
                                             View viewIncQty, View btnAddToBasket,
                                             Product product, String qty, @Nullable View productView,
                                             @Nullable HashMap<String, Integer> cartInfo);

    void updateUIAfterBasketOperationFailed(BasketOperation basketOperation, TextView basketCountTextView, View viewDecQty,
                                            View viewIncQty, View btnAddToBasket,
                                            Product product, String qty, String errorType, @Nullable View productView);
}
