package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.cart.CartSummary;

public interface CartInfoAware {

    void setCartInfo(CartSummary cartInfo);

    CartSummary getCartInfo();

    void updateUIForCartInfo();

    void markBasketDirty();

    void syncBasket();
}
