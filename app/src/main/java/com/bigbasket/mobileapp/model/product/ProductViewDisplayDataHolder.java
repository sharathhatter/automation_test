package com.bigbasket.mobileapp.model.product;

import android.graphics.Typeface;
import android.os.Handler;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;

public class ProductViewDisplayDataHolder {
    private Typeface sanSerifLightTypeface;
    private Typeface serifTypeface;
    private Typeface sansSerifMediumTypeface;
    private Typeface rupeeTypeface;
    private boolean showShoppingListBtn;
    private boolean isLoggedInMember;
    private boolean showBasketBtn;
    private boolean showShopListDeleteBtn;
    private Handler handler;
    private ShoppingListName shoppingListName;

    public ProductViewDisplayDataHolder(Typeface sanSerifLightTypeface, Typeface serifTypeface,
                                        Typeface sansSerifMediumTypeface, Typeface rupeeTypeface,
                                        boolean showShoppingListBtn, boolean isLoggedInMember,
                                        boolean showBasketBtn, boolean showShopListDeleteBtn, Handler handler) {
        this.sanSerifLightTypeface = sanSerifLightTypeface;
        this.serifTypeface = serifTypeface;
        this.sansSerifMediumTypeface = sansSerifMediumTypeface;
        this.rupeeTypeface = rupeeTypeface;
        this.showShoppingListBtn = showShoppingListBtn;
        this.isLoggedInMember = isLoggedInMember;
        this.showBasketBtn = showBasketBtn;
        this.showShopListDeleteBtn = showShopListDeleteBtn;
        this.handler = handler;
    }

    public ProductViewDisplayDataHolder(Typeface sanSerifLightTypeface,
                                        Typeface serifTypeface, Typeface sansSerifMediumTypeface,
                                        Typeface rupeeTypeface, boolean showShoppingListBtn, boolean isLoggedInMember,
                                        boolean showBasketBtn, boolean showShopListDeleteBtn, Handler handler,
                                        ShoppingListName shoppingListName) {
        this(sanSerifLightTypeface, serifTypeface, sansSerifMediumTypeface, rupeeTypeface, showShoppingListBtn,
                isLoggedInMember, showBasketBtn, showShopListDeleteBtn, handler);
        this.shoppingListName = shoppingListName;
    }

    public ShoppingListName getShoppingListName() {
        return shoppingListName;
    }

    public boolean showShopListDeleteBtn() {
        return showShopListDeleteBtn;
    }

    public Typeface getSerifTypeface() {
        return serifTypeface;
    }

    public Typeface getSansSerifMediumTypeface() {
        return sansSerifMediumTypeface;
    }

    public Typeface getRupeeTypeface() {
        return rupeeTypeface;
    }

    public boolean isShowShoppingListBtn() {
        return showShoppingListBtn;
    }

    public boolean isLoggedInMember() {
        return isLoggedInMember;
    }

    public boolean isShowBasketBtn() {
        return showBasketBtn;
    }

    public Handler getHandler() {
        return handler;
    }
}
