package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.cart.CartSummary;

public interface CartInfoAware {

    CartSummary getCartInfo();

    void setCartInfo(CartSummary cartInfo);

    void updateUIForCartInfo();

    void markBasketDirty();

    void syncBasket();
}
