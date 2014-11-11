package com.bigbasket.mobileapp.interfaces;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.product.Product;

public interface BasketOperationAware {
    void setBasketOperationResponse(BasketOperationResponse basketOperationResponse);

    void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView, ImageView imgDecQty,
                                             ImageView imgIncQty, Button btnAddToBasket, EditText editTextQty,
                                             Product product, String qty);

    void updateUIAfterBasketOperationFailed(BasketOperation basketOperation, TextView basketCountTextView, ImageView imgDecQty,
                                            ImageView imgIncQty, Button btnAddToBasket, EditText editTextQty,
                                            Product product, String qty);
}
