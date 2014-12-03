package com.bigbasket.mobileapp.util;

import android.text.TextUtils;
import android.util.Log;

import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.account.UpdateProfileModel;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderMonthRange;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.model.promo.ProductPromoInfo;
import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.slot.Slot;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class ParserUtil {
    private static final String TAG = ParserUtil.class.getName();

    public static ArrayList<TopCategoryModel> parseTopCategory(JsonArray topCategoryArray) {
        ArrayList<TopCategoryModel> topCategoryModels;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<TopCategoryModel>>() {
        }.getType();
        topCategoryModels = gson.fromJson(topCategoryArray, collectionType);
        return topCategoryModels;
    }

    public static OrderSummary parseOrderSummary(JsonObject orderSummaryJsonObj) {
        Gson gson = new Gson();
        return gson.fromJson(orderSummaryJsonObj, OrderSummary.class);
    }

    public static ArrayList<Product> parseProductList(String productListStr) {
        ArrayList<Product> products = null;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Product>>() {
        }.getType();
        products = gson.fromJson(productListStr, collectionType);
        return products;
    }

    public static Product parseProduct(JsonObject productJsonObj, String baseImgUrl) {
        Gson gson = new Gson();
        Product product = gson.fromJson(productJsonObj, Product.class);
        if (baseImgUrl != null) {
            product.setImageUrl(baseImgUrl + product.getImageUrl());
        }
        return product;
    }

    private static Product parseProductData(JSONObject productData, String baseImgUrl) {
        Product product = new Product();
        try {
            String productUrl = !TextUtils.isEmpty(baseImgUrl) ? baseImgUrl +
                    productData.optString(Constants.IMAGE_URL) :
                    productData.optString(Constants.IMAGE_URL);
            // product description
            product.setDescription(productData.optString(Constants.PRODUCT_DESC));
            // sell price
            product.setSellPrice(productData.optString(Constants.SELL_PRICE));
            // brand
            product.setBrand(productData.optString(Constants.PRODUCT_BRAND));
            // mrp
            product.setMrp(productData.optString(Constants.MRP_PRICE));
            // discount value
            product.setDiscountValue(productData.optString(Constants.DISCOUNT_VALUE));
            // discount type
            product.setDiscountType(productData.optString(Constants.DISCOUNT_TYPE));
            // package description
            product.setPackageDescription(productData.optString(Constants.PACKAGE_DESC));
            // no of items in the cart
            product.setNoOfItemsInCart(productData.optInt(Constants.NO_ITEM_IN_CART));
            // sku
            product.setSku(productData.optString(Constants.PRODUCT_ID));
            // weight
            product.setWeight(productData.optString(Constants.PRODUCT_WEIGHT));
            //is_new product type
            product.setNewProduct(productData.optBoolean(Constants.PRODUCT_IS_NEW));

            // is_bby pruduct
            product.setBbyProduct(productData.optBoolean(Constants.PRODUCT_IS_BBY));

            // image url
            product.setImageUrl(productUrl);
            // top level category slug
            product.setTopLevelCategorySlug(productData.optString(Constants.PRODUCT_TOP_LEVEL_CATEGORY_SLUG));
            // top level category name
            product.setTopLevelCategoryName(productData.optString(Constants.PRODUCT_TOP_LEVEL_CATEGORY_NAME));
            // product status
            product.setProductStatus(productData.optString(Constants.PRODUCT_STATUS));

            if (!productData.isNull(Constants.PRODUCT_PROMO_INFO) &&
                    productData.getJSONObject(Constants.PRODUCT_PROMO_INFO).length() > 0) {
                JSONObject productPromoInfo =
                        productData.getJSONObject(Constants.PRODUCT_PROMO_INFO);
                product.setProductPromoInfo(new ProductPromoInfo(
                        productPromoInfo.getString(Constants.PROMO_NAME),
                        productPromoInfo.optString(Constants.PROMO_ICON),  //promo Icon remove
                        productPromoInfo.getInt(Constants.PROMO_ID),
                        productPromoInfo.getString(Constants.PROMO_TYPE),
                        productPromoInfo.getString(Constants.PROMO_LABEL),
                        productPromoInfo.getString(Constants.PROMO_DESC),
                        productPromoInfo.getDouble(Constants.PROMO_SAVING)
                ));
            }

            // all products
            if (productData.has(Constants.PRODUCT_CHILD_PRODUCTS)) {
                JSONArray childProductsJsonArray = productData.getJSONArray(Constants.PRODUCT_CHILD_PRODUCTS);
                List<Product> childProducts = product.getAllProducts();
                if (childProducts == null) {
                    childProducts = new ArrayList<>();
                    product.setAllProducts(childProducts);
                }
                for (int index = 0; index < childProductsJsonArray.length(); index++) {
                    childProducts.add(parseProductData(childProductsJsonArray.getJSONObject(index),
                            baseImgUrl));
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return product;
    }

    public static COReserveQuantity parseCoReserveQuantity(String coReserveQuantityJson) {
        try {
            JSONObject coReserveQtyJsonObj = new JSONObject(coReserveQuantityJson);
            return parseCoReserveQuantity(coReserveQtyJsonObj);
        } catch (JSONException e) {
            return null;
        }
    }

    public static COReserveQuantity parseCoReserveQuantity(JSONObject coReserveQuantityJson) {
        COReserveQuantity result = new COReserveQuantity();
        result.setStatus(coReserveQuantityJson.optString(Constants.QC_STATUS).equals("OK"));
        JSONObject responseJsonObject = coReserveQuantityJson.optJSONObject(Constants.QC_RESPONSE);
        if (responseJsonObject != null) {
            result.setOrderId(responseJsonObject.optInt(Constants.QC_ORDER_ID));
            result.setQcHasErrors(responseJsonObject.optBoolean(Constants.QC_HAS_VALIDATION_ERRORS));

            if (result.isQcHasErrors()) {
                JSONArray productJsonArray = responseJsonObject.optJSONArray(Constants.QC_VALIDATION_ERROR_DATA);
                if (productJsonArray != null && productJsonArray.length() > 0) {
                    result.setQc_len(productJsonArray.length());
                    for (int i = 0; i < productJsonArray.length(); i++) {
                        JSONObject productJsonObject = productJsonArray.optJSONObject(i);
                        if (productJsonObject != null) {
                            QCErrorData qcErrorData = new QCErrorData(productJsonObject.optString(Constants.QC_RESERVED_QUANTITY), productJsonObject
                                    .optString(Constants.QC_ORIGINAL_QUANTITY),
                                    parseProductData(productJsonObject.optJSONObject(Constants.QC_RESERVED_PRODUCT), null)
                            );
                            result.getQCErrorData().add(qcErrorData);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static ArrayList<ShoppingListName> parseShoppingList(JsonObject jsonObject) {
        ArrayList<ShoppingListName> shoppingListNames = null;

        String success = jsonObject.get(Constants.SHOPPING_STATUS).getAsString();
        if (success != null && success.equals("OK")) {
            JsonArray shoppingListNamesJsonArray = jsonObject.getAsJsonArray(Constants.SHOPPING_LISTS);
            if (shoppingListNamesJsonArray != null) {
                shoppingListNames = parseShoppingList(shoppingListNamesJsonArray);
            }
        }
        return shoppingListNames;
    }

    public static ArrayList<ShoppingListName> parseShoppingList(JsonArray shoppingListNamesJsonArray) {
        ArrayList<ShoppingListName> shoppingListNames = new ArrayList<>();
        for (JsonElement shopJsonElement : shoppingListNamesJsonArray) {
            JsonObject shopJsonObj = shopJsonElement.getAsJsonObject();
            shoppingListNames.add(parseShoppingListName(shopJsonObj));
        }
        return shoppingListNames;
    }

    private static ShoppingListName parseShoppingListName(JsonObject jsonObject) {
        ShoppingListName shoppingListName = new ShoppingListName();
        shoppingListName.setName(jsonObject.get(Constants.SHOPPING_LIST_NAME).getAsString());
        shoppingListName.setSlug(jsonObject.get(Constants.SHOPPING_LIST_SLUG).getAsString());
        shoppingListName.setSystem(jsonObject.get(Constants.SHOPPING_LIST_IS_SYSTEM).getAsInt() == 1);
        return shoppingListName;
    }

    public static CartSummary parseGetCartCountResponse(JSONObject object) {
        CartSummary cartInfo = null;
        String status = object.optString(Constants.STATUS);
        if (status != null && status.equals("OK")) {
            cartInfo = new CartSummary();
            JSONObject responseJsonObject = object.optJSONObject(Constants.RESPONSE);
            if (responseJsonObject != null) {
                JSONObject summaryJsonObject = responseJsonObject.optJSONObject(Constants.CART_SUMMARY);
                String savingStr = summaryJsonObject.optString(Constants.CART_INFO_SAVING);
                String totalStr = summaryJsonObject.optString(Constants.CART_INFO_TOTAL);
                cartInfo.setTotal(Double.parseDouble(totalStr != null ? totalStr : "0"));
                cartInfo.setSavings(Double.parseDouble(savingStr != null ? savingStr : "0"));
                cartInfo.setNoOfItems(summaryJsonObject.optInt(Constants.CART_INFO_NUM_OF_ITEMS));
                cartInfo.setNotSupported(summaryJsonObject.optBoolean(Constants.IS_NOT_SUPPORTED, false));
                cartInfo.setKonotorEnabled(summaryJsonObject.optBoolean(Constants.ENABLE_KONOTOR, false));
                //cartInfo.setForceUpdate(summaryJsonObject.optBoolean(Constants.IS_FORCE_UPDATE, false));
                cartInfo.setIsUpdateRequired(summaryJsonObject.optInt(Constants.IS_UPDATE_REQUIRED));
                cartInfo.setAppExpireBy(summaryJsonObject.optString(Constants.APP_EXPIRE_BY));
                cartInfo.setHasExpressShops(summaryJsonObject.optBoolean(Constants.HAS_EXPRESS, false));
            }
        }
        return cartInfo;
    }

    public static CartSummary parseGetCartSummaryResponse(JsonObject cartJsonobject) {
        CartSummary cartInfo = new CartSummary();
        try {
            if (cartJsonobject != null) {
                String savingStr = cartJsonobject.get(Constants.CART_INFO_SAVING).getAsString();
                String totalStr = cartJsonobject.get(Constants.CART_INFO_TOTAL).getAsString();
                cartInfo.setTotal(Double.parseDouble(totalStr != null ? totalStr : "0"));
                cartInfo.setSavings(Double.parseDouble(savingStr != null ? savingStr : "0"));
                cartInfo.setNoOfItems(cartJsonobject.get(Constants.CART_INFO_NUM_OF_ITEMS).getAsInt());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cartInfo;
    }

    public static CartSummary parseCartSummaryFromJSON(JSONObject cartSummaryJsonObj) {
        try {
            CartSummary cartSummary = new CartSummary();
            if (cartSummaryJsonObj == null) return null;

            String savingStr = cartSummaryJsonObj.optString(Constants.CART_INFO_SAVING);
            String numItemsStr = cartSummaryJsonObj.getString(Constants.NUM_ITEMS);
            String totalStr = cartSummaryJsonObj.getString(Constants.TOTAL);

            cartSummary.setSavings(TextUtils.isEmpty(savingStr) ? 0 : Double.parseDouble(savingStr));
            cartSummary.setTotal(TextUtils.isEmpty(totalStr) ? 0 : Double.parseDouble(totalStr));
            cartSummary.setNoOfItems(TextUtils.isEmpty(numItemsStr) ? 0 : Integer.parseInt(numItemsStr));
            return cartSummary;
        } catch (JSONException e) {
            return null;
        }
    }

    public static ArrayList<WalletDataItem> getListData(String resp) {
        ArrayList<WalletDataItem> results = new ArrayList<WalletDataItem>();
        try {
            JSONArray arr = new JSONArray(resp);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject tempObj = arr.getJSONObject(i);
                WalletDataItem item = new WalletDataItem();
                item.setOrderId(tempObj.optString("order_id", null));
                item.setStartingBalance(tempObj.getDouble("starting_balance"));
                item.setEndingBalance(tempObj.getDouble("ending_balance"));
                item.setDate(tempObj.get("date").toString());
                item.setAmount(tempObj.getDouble("amount"));
                item.setPrimary_reason(tempObj.get("primary_reason").toString());
                item.setSecondary_reason(tempObj.get("secondary_reason").toString());
                item.setType(tempObj.get("type").toString());
                item.setOrderNumber(tempObj.optString("order_number", null));
                results.add(item);
            }
        } catch (JSONException e) {
            Log.e("Error", "JSONObject Error");
        }
        return results;
    }

    public static ArrayList<ActiveVouchers> parseActiveVouchersList(JSONArray activeVouchersJsonArray) {
        ArrayList<ActiveVouchers> activeVouchersList = new ArrayList<>();
        for (int i = 0; i < activeVouchersJsonArray.length(); i++) {
            try {
                JSONObject jsonObject = activeVouchersJsonArray.getJSONObject(i);
                activeVouchersList.add(new ActiveVouchers(jsonObject.getString(Constants.CODE),
                        jsonObject.getString(Constants.CUSTOMER_DESC), jsonObject.getString(Constants.MESSAGE),
                        jsonObject.getString(Constants.VALIDITY), jsonObject.getBoolean(Constants.CAN_APPLY)));
            } catch (JSONException e) {
            }
        }
        return activeVouchersList;
    }

    public static ArrayList<Address> parseAddressList(String jsonArrayStr) {
        ArrayList<Address> list = null;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Address>>() {
        }.getType();
        list = gson.fromJson(jsonArrayStr, collectionType);
        return list;
    }

    public static ArrayList<SlotGroup> parseSlotsList(JSONArray slotListJsonArray) {
        ArrayList<SlotGroup> slotsList = new ArrayList<>();
        for (int i = 0; i < slotListJsonArray.length(); i++) {
            try {
                JSONObject jsonObject = slotListJsonArray.getJSONObject(i);
                JSONObject jsonObjectFulfillment = jsonObject.getJSONObject(Constants.FULFILLMENT_INFO);
                JSONArray slotsJsonArray = jsonObject.optJSONArray(Constants.SLOTS);

                FulfillmentInfo fulfillmentInfo = new FulfillmentInfo(jsonObjectFulfillment);
                List<Slot> slotInGroupList = null;
                Slot selectedSlot = null, nextAvailableSlot = null;
                if (slotsJsonArray != null) {
                    slotInGroupList = parseSlot(slotsJsonArray);
                }
                JSONObject selectedSlotJsonObj = jsonObject.optJSONObject(Constants.SLOT);
                if (selectedSlotJsonObj != null) {
                    selectedSlot = new Slot(selectedSlotJsonObj);
                }
                JSONObject nextAvailableSlotJsonObj = jsonObject.optJSONObject(Constants.NEXT_AVAILABLE_SLOT);
                if (nextAvailableSlotJsonObj != null) {
                    nextAvailableSlot = new Slot(nextAvailableSlotJsonObj);
                }
                slotsList.add(new SlotGroup(fulfillmentInfo, slotInGroupList, selectedSlot, nextAvailableSlot));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return slotsList;
    }

    public static ArrayList<OrderMonthRange> parseOrderMonthList(JsonArray jsonArray) {
        ArrayList<OrderMonthRange> orderMonthRanges;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<OrderMonthRange>>() {
        }.getType();
        orderMonthRanges = gson.fromJson(jsonArray, collectionType);
        return orderMonthRanges;
    }

    public static ArrayList<Order> parseOrderList(JsonArray ordersJsonArray) {
        ArrayList<Order> orderArrayList;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Order>>() {
        }.getType();
        orderArrayList = gson.fromJson(ordersJsonArray, collectionType);
        return orderArrayList;
    }

    public static List<Slot> parseSlot(JSONArray slotJsonArray) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < slotJsonArray.length(); i++) {
            try {
                JSONObject jsonObject = slotJsonArray.getJSONObject(i);
                slots.add(new Slot(jsonObject));
            } catch (JSONException e) {
            }
        }
        return slots;
    }

    public static UpdateProfileModel parseUpdateProfileData(JsonObject jsonObjectUpdateProfile) {
        Gson gson = new Gson();
        UpdateProfileModel updateProfileModel = gson.fromJson(jsonObjectUpdateProfile, UpdateProfileModel.class);
        return updateProfileModel;
    }

    public static LinkedHashMap<String, String> parsePaymentTypes(JSONArray paymentTypeJsonArray) {
        try {
            LinkedHashMap<String, String> paymentTypeMap = new LinkedHashMap<>();
            for (int i = 0; i < paymentTypeJsonArray.length(); i++) {
                JSONObject paymentJsonObj = paymentTypeJsonArray.getJSONObject(i);
                paymentTypeMap.put(paymentJsonObj.getString(Constants.DISPLAY_NAME),
                        paymentJsonObj.getString(Constants.VALUE));
            }
            return paymentTypeMap;
        } catch (JSONException e) {
            return null;
        }
    }

    public static AutoSearchResponse parseAutoSearchResponse(JsonObject termsJsonObj) {
        AutoSearchResponse autoSearchResponse;
        Gson gson = new Gson();
        autoSearchResponse = gson.fromJson(termsJsonObj, AutoSearchResponse.class);
        return autoSearchResponse;
    }

    public static OrderInvoice parseOrderInvoice(JsonObject orderInvoiceJsonObj) {
        OrderInvoice orderInvoice;
        Gson gson = new Gson();
        orderInvoice = gson.fromJson(orderInvoiceJsonObj, OrderInvoice.class);
        return orderInvoice;
    }
}
