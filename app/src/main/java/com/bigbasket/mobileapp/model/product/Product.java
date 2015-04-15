package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.promo.ProductPromoInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Product implements Parcelable {

    @SerializedName(Constants.PRODUCT_DESC)
    private String description;

    @SerializedName(Constants.SELL_PRICE)
    private String sellPrice;

    @SerializedName(Constants.PRODUCT_BRAND)
    private String brand;

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

    @SerializedName(Constants.IMAGE_URL)
    private String imageUrl;

    @SerializedName(Constants.PRODUCT_TOP_LEVEL_CATEGORY_SLUG)
    private String topLevelCategorySlug;

    @SerializedName(Constants.PRODUCT_TOP_LEVEL_CATEGORY_NAME)
    private String topLevelCategoryName;

    @SerializedName(Constants.PRODUCT_STATUS)
    private String productStatus;

    @SerializedName(Constants.PRODUCT_CHILD_PRODUCTS)
    private List<Product> allProducts;

    @SerializedName(Constants.PRODUCT_IS_NEW)
    private boolean newProduct;

    @SerializedName(Constants.PRODUCT_IS_BBY)
    private boolean bbyProduct;

    @SerializedName(Constants.PRODUCT_PROMO_INFO)
    private ProductPromoInfo productPromoInfo;

    @SerializedName(Constants.ADDITIONAL_INFOS)
    private ArrayList<ProductAdditionalInfo> productAdditionalInfos;

    @SerializedName(Constants.PRODUCT_CATEGORY_NAME)
    private String productCategoryName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        boolean isSellPriceNull = sellPrice == null;
        dest.writeByte(isSellPriceNull ? (byte) 1 : (byte) 0);
        if (!isSellPriceNull) {
            dest.writeString(sellPrice);
        }
        dest.writeString(brand);
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
        dest.writeString(imageUrl);
        dest.writeString(topLevelCategorySlug);
        dest.writeString(productCategoryName);
        dest.writeString(topLevelCategoryName);
        dest.writeString(productStatus);
        boolean isChildProductsNull = allProducts == null;
        dest.writeByte(isChildProductsNull ? (byte) 1 : (byte) 0);
        if (!isChildProductsNull) {
            dest.writeTypedList(allProducts);
        }
        dest.writeByte(newProduct ? (byte) 1 : (byte) 0);
        dest.writeByte(bbyProduct ? (byte) 1 : (byte) 0);
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
    }

    public Product(Parcel source) {
        description = source.readString();
        boolean isSellPriceNull = source.readByte() == (byte) 1;
        if (!isSellPriceNull) {
            sellPrice = source.readString();
        }
        brand = source.readString();
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
        imageUrl = source.readString();
        topLevelCategorySlug = source.readString();
        productCategoryName = source.readString();
        topLevelCategoryName = source.readString();
        productStatus = source.readString();
        boolean isChildProductsNull = source.readByte() == (byte) 1;
        if (!isChildProductsNull) {
            allProducts = new ArrayList<>();
            source.readTypedList(allProducts, Product.CREATOR);
        }
        newProduct = source.readByte() == (byte) 1;
        bbyProduct = source.readByte() == (byte) 1;
        boolean isProductPromoInfoNull = source.readByte() == (byte) 1;
        if (!isProductPromoInfoNull) {
            productPromoInfo = source.readParcelable(Product.class.getClassLoader());
        }
        boolean isProductAdditionInfoNull = source.readByte() == (byte) 1;
        if (!isProductAdditionInfoNull) {
            productAdditionalInfos = new ArrayList<>();
            source.readTypedList(productAdditionalInfos, ProductAdditionalInfo.CREATOR);
        }
    }

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

    public Product() {
    }

    public boolean isBbyProduct() {
        return bbyProduct;
    }

    public void setBbyProduct(boolean bbyProduct) {
        this.bbyProduct = bbyProduct;
    }

    public boolean isNewProduct() {
        return newProduct;
    }

    public void setNewProduct(boolean newProduct) {
        this.newProduct = newProduct;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(String sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getMrp() {
        return mrp;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }

    public String getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(String discountValue) {
        this.discountValue = discountValue;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public String getPackageDescription() {
        return packageDescription;
    }

    public void setPackageDescription(String packageDescription) {
        this.packageDescription = packageDescription;
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

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getWeight() {
        return weight;
    }

    public String getWeightAndPackDesc() {
        return weight + (TextUtils.isEmpty(packageDescription) ? "" : " - " + packageDescription);
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTopLevelCategorySlug() {
        return topLevelCategorySlug;
    }

    public void setTopLevelCategorySlug(String topLevelCategorySlug) {
        this.topLevelCategorySlug = topLevelCategorySlug;
    }

    public String getTopLevelCategoryName() {
        return topLevelCategoryName;
    }

    public void setTopLevelCategoryName(String topLevelCategoryName) {
        this.topLevelCategoryName = topLevelCategoryName;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public void setProductCategoryName(String productCategoryName) {
        this.productCategoryName = productCategoryName;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public List<Product> getAllProducts() {
        return allProducts;
    }

    public void setAllProducts(List<Product> allProducts) {
        this.allProducts = allProducts;
    }

    public ProductPromoInfo getProductPromoInfo() {
        return productPromoInfo;
    }

    public void setProductPromoInfo(ProductPromoInfo productPromoInfo) {
        this.productPromoInfo = productPromoInfo;
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
        return brand + " " + description + " " + weight + "  Rs. " + sellPrice;
    }

    public static boolean areAllProductsOutOfStock(List<Product> productList) {
        for (Product product : productList) {
            if (product != null && product.getProductStatus().equalsIgnoreCase("A")) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<ProductAdditionalInfo> getProductAdditionalInfos() {
        return productAdditionalInfos;
    }


    public Product(String brand, String description, String sku, String topLevelCategoryName,
                   String productCategoryName) {
        this.brand = brand;
        this.description = description;
        this.sku = sku;
        this.topLevelCategoryName = topLevelCategoryName;
        this.productCategoryName = productCategoryName;
    }
}