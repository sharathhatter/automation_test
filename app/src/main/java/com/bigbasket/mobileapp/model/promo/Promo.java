package com.bigbasket.mobileapp.model.promo;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class Promo implements Parcelable {

    public final class PromoType {
        private PromoType() {
        }

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

    }

    public static String[] getAllTypes() {
        String[] allTypes = new String[9];
        allTypes[0] = PromoType.FREE;
        allTypes[1] = PromoType.DISCOUNT_PRICE;
        allTypes[2] = PromoType.CUSTOMIZED_COMBO;
        allTypes[3] = PromoType.FIXED_COMBO;
        allTypes[4] = PromoType.MIN_ORDER_DISCOUNTED_PRODUCT;
        allTypes[5] = PromoType.MIN_ORDER_FREE_PRODUCT;
        allTypes[6] = PromoType.DISCOUNTED_BIN;
        allTypes[7] = PromoType.FIXED_FREE_COMBO;
        allTypes[8] = PromoType.MEMBER_REFERRAL;
        return allTypes;
    }

    @SerializedName(Constants.PROMO_NAME)
    protected String promoName;

    @SerializedName(Constants.PROMO_ICON)
    protected String promoIcon;

    @SerializedName(Constants.PROMO_ID)
    protected int id;

    @SerializedName(Constants.PROMO_TYPE)
    private String promoType;

    @SerializedName(Constants.PROMO_LABEL)
    protected String promoLabel;

    @SerializedName(Constants.PROMO_DESC)
    private String promoDesc;

    @SerializedName(Constants.PROMO_DESC_LINE1)
    private String promoDescLine1;

    @SerializedName(Constants.PROMO_DESC_LINE2)
    private String promoDescLine2;

    private PromoCategory promoCategory;

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

    public Promo(String promoName, String promoIcon, int id, String promoType,
                 String promoLabel, String promoDesc) {
        this.promoName = promoName;
        this.id = id;
        this.promoType = promoType;
        this.promoDesc = promoDesc;
        this.promoLabel = promoLabel;
        this.promoIcon = promoIcon;
    }

    public String getPromoLabel() {
        return promoLabel;
    }

    public void setPromoLabel(String promoLabel) {
        this.promoLabel = promoLabel;
    }

    public String getPromoType() {
        return promoType;
    }

    public void setPromoType(String promoType) {
        this.promoType = promoType;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
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

    public void setPromoDesc(String promoDesc) {
        this.promoDesc = promoDesc;
    }

    public String getPromoIcon() {
        return promoIcon;
    }

    public void setPromoIcon(String promoIcon) {
        this.promoIcon = promoIcon;
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
}
