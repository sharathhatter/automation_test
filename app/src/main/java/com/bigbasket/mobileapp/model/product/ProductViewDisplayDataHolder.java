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

    public static class Builder {
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

        public Builder() {
        }

        public Builder setCommonTypeface(Typeface commonTypeface) {
            this.sanSerifLightTypeface = commonTypeface;
            this.serifTypeface = commonTypeface;
            this.sansSerifMediumTypeface = commonTypeface;
            return this;
        }

        public Builder setRupeeTypeface(Typeface rupeeTypeface) {
            this.rupeeTypeface = rupeeTypeface;
            return this;
        }

        public Builder setShowShoppingListBtn(boolean showShoppingListBtn) {
            this.showShoppingListBtn = showShoppingListBtn;
            return this;
        }

        public Builder setLoggedInMember(boolean isLoggedInMember) {
            this.isLoggedInMember = isLoggedInMember;
            return this;
        }

        public Builder setShowBasketBtn(boolean showBasketBtn) {
            this.showBasketBtn = showBasketBtn;
            return this;
        }

        public Builder setShowShopListDeleteBtn(boolean showShopListDeleteBtn) {
            this.showShopListDeleteBtn = showShopListDeleteBtn;
            return this;
        }

        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public Builder setShoppingListName(ShoppingListName shoppingListName) {
            this.shoppingListName = shoppingListName;
            return this;
        }

        public ProductViewDisplayDataHolder build() {
            return new ProductViewDisplayDataHolder(this);
        }
    }

    public ProductViewDisplayDataHolder(Builder builder) {
        this.sanSerifLightTypeface = builder.sanSerifLightTypeface;
        this.serifTypeface = builder.serifTypeface;
        this.sansSerifMediumTypeface = builder.sansSerifMediumTypeface;
        this.rupeeTypeface = builder.rupeeTypeface;
        this.showShoppingListBtn = builder.showShoppingListBtn;
        this.isLoggedInMember = builder.isLoggedInMember;
        this.showBasketBtn = builder.showBasketBtn;
        this.showShopListDeleteBtn = builder.showShopListDeleteBtn;
        this.handler = builder.handler;
        this.shoppingListName = builder.shoppingListName;
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

