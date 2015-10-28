package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.product.Product;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public interface BasketOperationAware {
    void setBasketOperationResponse(BasketOperationResponse basketOperationResponse);

    void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation,
                                             @Nullable WeakReference<TextView> basketCountTextViewRef,
                                             @Nullable WeakReference<View> viewDecQtyRef,
                                             @Nullable WeakReference<View> viewIncQtyRef,
                                             @Nullable WeakReference<View> btnAddToBasketRef,
                                             Product product, String qty,
                                             @Nullable WeakReference<View> productViewRef,
                                             @Nullable WeakReference<HashMap<String, Integer>> cartInfoMapRef,
                                             @Nullable WeakReference<EditText> editTextQtyRef);

    void updateUIAfterBasketOperationFailed(@BasketOperation.Mode int basketOperation,
                                            @Nullable WeakReference<TextView> basketCountTextViewRef,
                                            @Nullable WeakReference<View> viewDecQtyRef,
                                            @Nullable WeakReference<View> viewIncQtyRef,
                                            @Nullable WeakReference<View> btnAddToBasketRef,
                                            Product product, String qty, String errorType,
                                            @Nullable WeakReference<View> productViewRef,
                                            @Nullable WeakReference<EditText> editTextQtyRef);
}
