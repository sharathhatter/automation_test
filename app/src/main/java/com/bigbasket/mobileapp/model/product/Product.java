package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.promo.ProductPromoInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Product extends BaseProduct {

    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
    @SerializedName(Constants.SELL_PRICE)
    private String sellPrice;
    @SerializedName(Constants.MRP_PRICE)
    private String mrp;
    @SerializedName(Constants.DISCOUNT_VALUE)
    private String discountValue;
    @SerializedName(Constants.DISCOUNT_TYPE)
    private String discountType;
    @SerializedName(Constants.PACKAGE_DESC)
    private String packageDescription;
    @SerializedName(Constants.NO_ITEM_IN_CART)
    private int noOfItemsInCart;
    @SerializedName(Constants.PRODUCT_ID)
    private String sku;
    @SerializedName(Constants.PRODUCT_WEIGHT)
    private String weight;
    @SerializedName(Constants.PRODUCT_TOP_LEVEL_CATEGORY_SLUG)
    private String topLevelCategorySlug;
    @SerializedName(Constants.PRODUCT_TOP_LEVEL_CATEGORY_NAME)
    private String topLevelCategoryName;
    @SerializedName(Constants.PRODUCT_STATUS)
    private String productStatus;
    @SerializedName(Constants.PRODUCT_CHILD_PRODUCTS)
    private List<Product> allProducts;
    @SerializedName(Constants.PRODUCT_PROMO_INFO)
    private ProductPromoInfo productPromoInfo;
    @SerializedName(Constants.ADDITIONAL_INFOS)
    private ArrayList<ProductAdditionalInfo> productAdditionalInfos;
    @SerializedName(Constants.PRODUCT_CATEGORY_NAME)
    private String productCategoryName;
    @SerializedName(Constants.BRAND_SLUG)
    private String brandSlug;
    @SerializedName(Constants.STORE_AVAILABILITY)
    private ArrayList<HashMap<String, String>> storeAvailability;
    @SerializedName(Constants.GIFT_MSG)
    private String giftMsg;
    @SerializedName(Constants.VARIABLE_WEIGHT_MSG)
    private String variableWeightMsg;
    @SerializedName(Constants.STORE_IDS)
    private List<String> storeIds;

    public Product(Parcel source) {
        super(source);
        boolean isSellPriceNull = source.readByte() == (byte) 1;

        if (!isSellPriceNull) {
            sellPrice = source.readString();
        }
        boolean isMrpNull = source.readByte() == (byte) 1;
        if (!isMrpNull) {
            mrp = source.readString();
        }
        discountValue = source.readString();
        discountType = source.readString();
        boolean isPkgDescNull = source.readByte() == (byte) 1;
        if (!isPkgDescNull) {
            packageDescription = source.readString();
        }
        noOfItemsInCart = source.readInt();
        sku = source.readString();
        weight = source.readString();
        topLevelCategorySlug = source.readString();
        productCategoryName = source.readString();
        topLevelCategoryName = source.readString();
        productStatus = source.readString();
        boolean isChildProductsNull = source.readByte() == (byte) 1;
        if (!isChildProductsNull) {
            allProducts =  source.createTypedArrayList(Product.CREATOR);
        }
        boolean isProductPromoInfoNull = source.readByte() == (byte) 1;
        if (!isProductPromoInfoNull) {
            productPromoInfo = source.readParcelable(Product.class.getClassLoader());
        }
        boolean isProductAdditionInfoNull = source.readByte() == (byte) 1;
        if (!isProductAdditionInfoNull) {
            productAdditionalInfos = source.createTypedArrayList(ProductAdditionalInfo.CREATOR);
        }
        boolean isBrandSlugNull = source.readByte() == (byte) 1;
        if (!isBrandSlugNull) {
            brandSlug = source.readString();
        }

        boolean isStoreAvailabilityNull = source.readByte() == (byte) 1;
        if (!isStoreAvailabilityNull) {
            String storeAvailabilityJson = source.readString();
            Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {
            }.getType();
            storeAvailability = new Gson().fromJson(storeAvailabilityJson, type);
        }

        boolean isGiftMsgNull = source.readByte() == (byte) 1;
        if (!isGiftMsgNull) {
            giftMsg = source.readString();
        }

        boolean isWeightMsgNull = source.readByte() == (byte) 1;
        if (!isWeightMsgNull) {
            variableWeightMsg = source.readString();
        }
        boolean isStoreIdsNull = source.readByte() == (byte) 1;
        if (!isStoreIdsNull) {
            storeIds = new ArrayList<>();
            source.readStringList(storeIds);
        }
    }

    public Product() {
    }

    public Product(String brand, String description, String sku, String topLevelCategoryName,
                   String productCategoryName, int noOfItemsInCart) {
        super(brand, description);
        this.sku = sku;
        this.topLevelCategoryName = topLevelCategoryName;
        this.productCategoryName = productCategoryName;
        this.noOfItemsInCart = noOfItemsInCart;
    }

    public static boolean areAllProductsOutOfStock(@Nullable List<Product> productList) {
        if (productList == null || productList.size() == 0) return true;
        for (Product product : productList) {
            if (product != null) {
                if (product.getStoreAvailability() != null && product.getStoreAvailability().size() > 0) {
                    for (HashMap<String, String> availabilityMap : product.getStoreAvailability()) {
                        if (availabilityMap.get(Constants.PRODUCT_STATUS) != null &&
                                availabilityMap.get(Constants.PRODUCT_STATUS).equalsIgnoreCase("A")) {
                            return false;
                        }
                    }
                } else if (!TextUtils.isEmpty(product.getProductStatus())
                        && product.getProductStatus().equals("A")) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        boolean isSellPriceNull = sellPrice == null;
        dest.writeByte(isSellPriceNull ? (byte) 1 : (byte) 0);
        if (!isSellPriceNull) {
            dest.writeString(sellPrice);
        }
        boolean isMrpNull = mrp == null;
        dest.writeByte(isMrpNull ? (byte) 1 : (byte) 0);
        if (!isMrpNull) {
            dest.writeString(mrp);
        }
        dest.writeString(discountValue);
        dest.writeString(discountType);
        boolean isPkgDescNull = packageDescription == null;
        dest.writeByte(isPkgDescNull ? (byte) 1 : (byte) 0);
        if (!isPkgDescNull) {
            dest.writeString(packageDescription);
        }
        dest.writeInt(noOfItemsInCart);
        dest.writeString(sku);
        dest.writeString(weight);
        dest.writeString(topLevelCategorySlug);
        dest.writeString(productCategoryName);
        dest.writeString(topLevelCategoryName);
        dest.writeString(productStatus);
        boolean isChildProductsNull = allProducts == null;
        dest.writeByte(isChildProductsNull ? (byte) 1 : (byte) 0);
        if (!isChildProductsNull) {
            dest.writeTypedList(allProducts);
        }
        boolean isProductPromoInfoNull = productPromoInfo == null;
        dest.writeByte(isProductPromoInfoNull ? (byte) 1 : (byte) 0);
        if (!isProductPromoInfoNull) {
            dest.writeParcelable(productPromoInfo, flags);
        }
        boolean isProductAdditionInfoNull = productAdditionalInfos == null;
        dest.writeByte(isProductAdditionInfoNull ? (byte) 1 : (byte) 0);
        if (!isProductAdditionInfoNull) {
            dest.writeTypedList(productAdditionalInfos);
        }
        boolean isBrandSlugNull = brandSlug == null;
        dest.writeByte(isBrandSlugNull ? (byte) 1 : (byte) 0);
        if (!isBrandSlugNull) {
            dest.writeString(brandSlug);
        }

        boolean isStoreAvailabilityNull = storeAvailability == null;
        dest.writeByte(isStoreAvailabilityNull ? (byte) 1 : (byte) 0);
        if (!isStoreAvailabilityNull) {
            dest.writeString(new Gson().toJson(storeAvailability));
        }

        boolean isGiftMsgNull = giftMsg == null;
        dest.writeByte(isGiftMsgNull ? (byte) 1 : (byte) 0);
        if (!isGiftMsgNull) {
            dest.writeString(giftMsg);
        }

        boolean isWeightMsgNull = variableWeightMsg == null;
        dest.writeByte(isWeightMsgNull ? (byte) 1 : (byte) 0);
        if (!isWeightMsgNull) {
            dest.writeString(variableWeightMsg);
        }
        boolean isStoredIdsNull = storeIds == null;
        dest.writeByte(isStoredIdsNull ? (byte) 1 : (byte) 0);
        if (!isStoredIdsNull) {
            dest.writeStringList(storeIds);
        }
    }

    public String getSellPrice() {
        return sellPrice;
    }

    public String getMrp() {
        return mrp;
    }

    public String getDiscountValue() {
        return discountValue;
    }

    public String getPackageDescription() {
        return packageDescription;
    }

    public int getNoOfItemsInCart() {
        return noOfItemsInCart;
    }

    public void setNoOfItemsInCart(int noOfItemsInCart) {
        this.noOfItemsInCart = noOfItemsInCart;
    }

    public String getSku() {
        return sku;
    }

    public String getWeight() {
        return weight;
    }

    public String getWeightAndPackDesc() {
        return weight + (TextUtils.isEmpty(packageDescription) ? "" : " - " + packageDescription);
    }

    public String getGiftMsg() {
        return giftMsg;
    }

    public String getTopLevelCategorySlug() {
        return topLevelCategorySlug;
    }

    public String getTopLevelCategoryName() {
        return topLevelCategoryName;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public List<Product> getAllProducts() {
        return allProducts;
    }

    public ProductPromoInfo getProductPromoInfo() {
        return productPromoInfo;
    }

    public double getActualDiscount() {
        double mrpVal = TextUtils.isEmpty(mrp) ? 0 : Double.parseDouble(mrp);
        double sellPriceVal = TextUtils.isEmpty(sellPrice) ? 0 : Double.parseDouble(sellPrice);
        double actualDiscount = mrpVal - sellPriceVal;
        actualDiscount = Math.abs(actualDiscount);
        return actualDiscount;
    }

    public boolean hasSavings() {
        return !discountValue.equals("") && getActualDiscount() != 0.0;
    }

    @Override
    public String toString() {
        return getBrand() + " " + getDescription() + " " + weight + "  Rs. " + sellPrice;
    }

    public ArrayList<ProductAdditionalInfo> getProductAdditionalInfos() {
        return productAdditionalInfos;
    }

    public String getBrandSlug() {
        return brandSlug;
    }

    @Nullable
    public ArrayList<HashMap<String, String>> getStoreAvailability() {
        return storeAvailability;
    }

    @Nullable
    public String getVariableWeightMsg() {
        return variableWeightMsg;
    }

    @Nullable
    public List<String> getStoreIds() {
        return storeIds;
    }
}