package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CartItemPromo extends BaseCartItem {

    @SerializedName(Constants.PROMO_ID)
    private int promoId;

    @SerializedName(Constants.PROMO_NAME)
    private String promoName;

    @SerializedName(Constants.NO_ITEM_IN_CART)
    private double numItemInCart;

    @SerializedName(Constants.PROMO_LABEL)
    protected String promo_label;

    public CartItemPromo() {
    }

    public CartItemPromo(double saving, double mrp, double salePrice,
                         int promoId, String promoName, double numItemInCart) {
        super(saving, mrp, salePrice);
        this.promoId = promoId;
        this.promoName = promoName;
        this.numItemInCart = numItemInCart;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(promoId);
        dest.writeString(promoName);
        dest.writeDouble(numItemInCart);
        dest.writeString(promo_label);
        dest.writeDouble(getMrp());
        dest.writeDouble(getSalePrice());
        dest.writeDouble(getSaving());
    }

    CartItemPromo(Parcel source) {
        promoId = source.readInt();
        promoName = source.readString();
        numItemInCart = source.readDouble();
        promo_label = source.readString();
        setMrp(source.readDouble());
        setSalePrice(source.readDouble());
        setSaving(source.readDouble());
    }

    public static final Parcelable.Creator<CartItemPromo> CREATOR = new Parcelable.Creator<CartItemPromo>() {
        @Override
        public CartItemPromo createFromParcel(Parcel source) {
            return new CartItemPromo(source);
        }

        @Override
        public CartItemPromo[] newArray(int size) {
            return new CartItemPromo[size];
        }
    };

    public String getPromo_label() {
        return promo_label;
    }

    public void setPromo_label(String promo_label) {
        this.promo_label = promo_label;
    }

    public int getPromoId() {
        return promoId;
    }

    public void setPromoId(int promoId) {
        this.promoId = promoId;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public double getNumItemInCart() {
        return numItemInCart;
    }

    public void setNumItemInCart(double numItemInCart) {
        this.numItemInCart = numItemInCart;
    }
}
