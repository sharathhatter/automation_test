package com.bigbasket.mobileapp.model.promo;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

public class Promo implements Parcelable {

    public static final Parcelable.Creator<Promo> CREATOR = new Parcelable.Creator<Promo>() {
        @Override
        public Promo createFromParcel(Parcel source) {
            return new Promo(source);
        }

        @Override
        public Promo[] newArray(int size) {
            return new Promo[size];
        }
    };
    @SerializedName(Constants.PROMO_NAME)
    protected String promoName;
    @SerializedName(Constants.PROMO_ICON)
    protected String promoIcon;
    @SerializedName(Constants.PROMO_ID)
    protected int id;
    @SerializedName(Constants.PROMO_LABEL)
    protected String promoLabel;
    @SerializedName(Constants.PROMO_TYPE)
    private String promoType;
    @SerializedName(Constants.PROMO_DESC)
    private String promoDesc;
    @SerializedName(Constants.PROMO_DESC_LINE1)
    private String promoDescLine1;
    @SerializedName(Constants.PROMO_DESC_LINE2)
    private String promoDescLine2;
    private PromoCategory promoCategory;

    public Promo(Parcel source) {
        promoName = source.readString();
        boolean isPromoIconNull = source.readByte() == (byte) 1;
        if (!isPromoIconNull) {
            promoIcon = source.readString();
        }
        id = source.readInt();
        promoType = source.readString();
        promoLabel = source.readString();
        promoDesc = source.readString();
        promoDescLine1 = source.readString();
        promoDescLine2 = source.readString();
    }

    public Promo(String promoName, String promoIcon, int id, String promoType,
                 String promoLabel, String promoDesc) {
        this.promoName = promoName;
        this.id = id;
        this.promoType = promoType;
        this.promoDesc = promoDesc;
        this.promoLabel = promoLabel;
        this.promoIcon = promoIcon;
    }

    public static Set<String> getAllTypes() {
        Set<String> promoTypes = new HashSet<>();
        promoTypes.add(PromoType.FREE);
        promoTypes.add(PromoType.DISCOUNT_PRICE);
        promoTypes.add(PromoType.CUSTOMIZED_COMBO);
        promoTypes.add(PromoType.FIXED_COMBO);
        promoTypes.add(PromoType.MIN_ORDER_DISCOUNTED_PRODUCT);
        promoTypes.add(PromoType.MIN_ORDER_FREE_PRODUCT);
        promoTypes.add(PromoType.DISCOUNTED_BIN);
        promoTypes.add(PromoType.FIXED_FREE_COMBO);
        promoTypes.add(PromoType.MEMBER_REFERRAL);
        return promoTypes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(promoName);
        boolean isPromoIconNull = promoIcon == null;
        dest.writeByte(isPromoIconNull ? (byte) 1 : (byte) 0);
        if (!isPromoIconNull) {
            dest.writeString(promoIcon);
        }
        dest.writeInt(id);
        dest.writeString(promoType);
        dest.writeString(promoLabel);
        dest.writeString(promoDesc);
        dest.writeString(promoDescLine1);
        dest.writeString(promoDescLine2);
    }

    public String getPromoLabel() {
        return promoLabel;
    }

    public String getPromoType() {
        return promoType;
    }

    public String getPromoName() {
        return promoName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPromoDesc() {
        return promoDesc;
    }

    public String getPromoIcon() {
        return promoIcon;
    }

    public String getPromoDescLine1() {
        return promoDescLine1;
    }

    public String getPromoDescLine2() {
        return promoDescLine2;
    }

    public PromoCategory getPromoCategory() {
        return promoCategory;
    }

    public void setPromoCategory(PromoCategory promoCategory) {
        this.promoCategory = promoCategory;
    }

    public final class PromoType {
        public static final String FREE = "free";
        public static final String DISCOUNT_PRICE = "discount_price";
        public static final String CUSTOMIZED_COMBO = "customized_combo";
        public static final String FIXED_COMBO = "fixed_combo";
        public static final String MIN_ORDER_DISCOUNTED_PRODUCT =
                "min_order_discounted_product";
        public static final String MIN_ORDER_FREE_PRODUCT = "min_order_free_product";
        public static final String DISCOUNTED_BIN = "discounted_bin";
        public static final String FIXED_FREE_COMBO = "fixed_free_combo";
        public static final String MEMBER_REFERRAL = "member_referral";

        private PromoType() {
        }

    }
}
