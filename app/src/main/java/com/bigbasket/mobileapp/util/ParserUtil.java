package com.bigbasket.mobileapp.util;

import android.text.TextUtils;
import android.util.Log;

import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.account.UpdateProfileModel;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderMonthRange;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.model.product.FilterOption;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.Option;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.model.promo.ProductPromoInfo;
import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.bigbasket.mobileapp.model.promo.PromoDetail;
import com.bigbasket.mobileapp.model.search.AutoSearchResponse;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static CartSummary parseCartSummary(JsonObject cartSummaryJsonObject) {
        CartSummary cartSummary;
        Gson gson = new Gson();
        cartSummary = gson.fromJson(cartSummaryJsonObject, CartSummary.class);
        return cartSummary;
    }

    public static ArrayList<CartItemList> parseCartItemList(JsonArray cartItemArray) {
        ArrayList<CartItemList> cartItemLists;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<CartItemList>>() {
        }.getType();
        cartItemLists = gson.fromJson(cartItemArray, collectionType);
        return cartItemLists;
    }

    public static ArrayList<FulfillmentInfo> parseCartFulfillmentInfoList(JsonArray cartFullfillmentArray) {
        ArrayList<FulfillmentInfo> fullfillmentInfos;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<FulfillmentInfo>>() {
        }.getType();
        fullfillmentInfos = gson.fromJson(cartFullfillmentArray, collectionType);
        return fullfillmentInfos;
    }

    public static ArrayList<AnnotationInfo> parseCartAnnotationInfoList(JsonArray cartAnnotationInfoArray) {
        ArrayList<AnnotationInfo> annotationInfoArrayList;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<AnnotationInfo>>() {
        }.getType();
        annotationInfoArrayList = gson.fromJson(cartAnnotationInfoArray, collectionType);
        return annotationInfoArrayList;
    }

    public static FulfillmentInfo parseFulfillmentInfo(JsonObject summaryFulfillmentInfoJsonObj) {
        Gson gson = new Gson();
        return gson.fromJson(summaryFulfillmentInfoJsonObj, FulfillmentInfo.class);
    }

    public static OrderSummary parseOrderSummary(JsonObject orderSummaryJsonObj) {
        Gson gson = new Gson();
        return gson.fromJson(orderSummaryJsonObj, OrderSummary.class);
    }

    public static ArrayList<PromoCategory> parsePromoCategory(JsonArray promoCategoryResponse) {
        ArrayList<PromoCategory> promoCategories = null;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<PromoCategory>>() {
        }.getType();
        promoCategories = gson.fromJson(promoCategoryResponse, collectionType);
        return promoCategories;
    }

    public static PromoDetail parsePromoDetail(JsonObject jsonObject) {
        PromoDetail promoDetail = null;
        JsonObject responseJsonObject =
                jsonObject.get(Constants.RESPONSE).getAsJsonObject();

        if (responseJsonObject != null) {
            JsonObject promoDetailJsonObject =
                    responseJsonObject.get(Constants.PROMO_DETAILS).getAsJsonObject();
            if (promoDetailJsonObject != null) {
                Gson gson = new Gson();
                promoDetail = gson.fromJson(promoDetailJsonObject, PromoDetail.class);
                String freeProducts = promoDetailJsonObject.has(Constants.FREE_PRODS) ?
                        promoDetailJsonObject.get(Constants.FREE_PRODS).toString() : null;
                String fixedComboProducts = promoDetailJsonObject.has(Constants.FIX_COMBO_PRODS)
                        ? promoDetailJsonObject.get(Constants.FIX_COMBO_PRODS).toString() : null;
                promoDetail.setFreeProducts(freeProducts);
                promoDetail.setFixedComboProducts(fixedComboProducts);
            }
        }
        return promoDetail;
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

    public static ProductListData parseProductListData(JSONObject productResponse) {
        ProductListData data = new ProductListData();
        try {
            JSONObject productListJsonData = productResponse.getJSONObject("response");
            String baseImgUrl = productListJsonData.optString(Constants.BASE_IMG_URL, null);
            if (productListJsonData.has(Constants.TOTAL_PAGES)) {
                data.setTotalPages(productListJsonData.optInt(Constants.TOTAL_PAGES));
            }

            // get the sort options
            ArrayList<Option> sortOptions = new ArrayList<>();
            String sortedOnSlug = productListJsonData.optString(Constants.SORT_ON);
            if (productListJsonData.has(Constants.PRODUCT_SORT_OPTION)) {
                JSONArray sortOptionJsonArray = productListJsonData.getJSONArray((Constants.PRODUCT_SORT_OPTION));
                JSONArray optionArray;
                for (int i = 0; i < sortOptionJsonArray.length(); i++) {
                    optionArray = sortOptionJsonArray.getJSONArray(i);
                    String sortListSlug = optionArray.getString(1);
                    String sortListDisplay = optionArray.getString(0);
                    sortOptions.add(new Option(sortListDisplay, sortListSlug));
                }
            }
            data.setSortOptions(sortOptions);
            data.setSortedOn(sortedOnSlug);

            // get the filter options
            ArrayList<FilterOptionCategory> filterOptionCategories = new ArrayList<>();
            Map<String, Set<String>> filteredOnOptions = new HashMap<>();
            JSONArray filterOptionJsonArray = productListJsonData.optJSONArray(Constants.FILTER_OPTIONS);
            if (filterOptionJsonArray != null) {
                for (int i = 0; i < filterOptionJsonArray.length(); i++) {
                    JSONObject filterJsonObject = filterOptionJsonArray.getJSONObject(i);
                    List<FilterOptionItem> filterOptionItems = new ArrayList<>();
                    FilterOption parentFilter = new FilterOption(filterJsonObject.getString(Constants.FILTER_NAME), filterJsonObject.getString(Constants.FILTER_SLUG), null, true);

                    filteredOnOptions.put(parentFilter.getFilterSlug(), new HashSet<String>());
                    JSONArray optionsJsonArray = filterJsonObject.getJSONArray(Constants.FILTER_VALUES);
                    for (int j = 0; j < optionsJsonArray.length(); j++) {
                        JSONObject optionJsonObject = optionsJsonArray.getJSONObject(j);
                        filterOptionItems.add(new FilterOptionItem(optionJsonObject.
                                getString(Constants.FILTER_VALUES_NAME),
                                optionJsonObject.getString(Constants.FILTER_VALUES_SLUG)));
                    }
                    filterOptionCategories.add(new FilterOptionCategory(filterJsonObject.
                            getString(Constants.FILTER_NAME),
                            filterJsonObject.getString(Constants.FILTER_SLUG), filterOptionItems));
                }
            }

            // populate filtered on options
            JSONArray filteredOnOptionJsonArray = productListJsonData.optJSONArray(Constants.FILTERED_ON);
            if (filteredOnOptionJsonArray != null) {
                for (int i = 0; i < filteredOnOptionJsonArray.length(); i++) {
                    JSONObject filterJsonObject = filteredOnOptionJsonArray.getJSONObject(i);
                    String filterSlug = filterJsonObject.getString(Constants.FILTER_SLUG);

                    JSONArray optionsJsonArray = filterJsonObject.getJSONArray(Constants.FILTER_VALUES);
                    for (int j = 0; j < optionsJsonArray.length(); j++) {
                        String subFilterSlug = optionsJsonArray.getString(j);
                        filteredOnOptions.get(filterSlug).add(subFilterSlug);
                    }
                }
            }

            // preselect the filter options
            for (FilterOptionCategory filterOptionCategory : filterOptionCategories) {
                if (filterOptionCategory.getFilterOptionItems() != null) {
                    for (FilterOptionItem filterOptionItem : filterOptionCategory.getFilterOptionItems()) {
                        if (filteredOnOptions.get(filterOptionCategory.getFilterSlug()).contains(filterOptionItem.getFilterValueSlug())) {
                            filterOptionItem.setSelected(true);
                        }
                    }
                }
            }
            data.setFilterOptions(filterOptionCategories);
            //data.setFilteredOn(filteredOnOptions);

            // current page
            data.setCurrentPage(productListJsonData.optInt(Constants.CURRENT_PAGE));
            // total number of records
            data.setProductCount(productListJsonData.getInt(Constants.PRODUCT_COUNT));
            // query text
            data.setQuery(productListJsonData.optString(Constants.SEARCH_QUERY));

            // parse the products
            JSONArray productJsonArray = productListJsonData.getJSONArray(Constants.PRODUCTS);
            List<Product> productList = parseProductJsonArray(productJsonArray, baseImgUrl);
            data.setProducts(productList);

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return data;
    }

    public static ArrayList<Product> parseProductJsonArray(JSONArray productJsonArray, String baseImgUrl) {
        ArrayList<Product> productList = new ArrayList<>();
        for (int index = 0; index < productJsonArray.length(); index++) {
            JSONObject productJsonObject;
            try {
                productJsonObject = productJsonArray.getJSONObject(index);
            } catch (JSONException e) {
                Log.e("ProductListAware", "Error while parsing product json");
                continue;
            }
            Product product = parseProductData(productJsonObject, baseImgUrl);
            productList.add(product);
        }
        return productList;
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

    public static ArrayList<ShoppingListSummary> parseShoppingListSummary(JsonArray shoppingListSummaryJsonArray) {
        if (shoppingListSummaryJsonArray == null || shoppingListSummaryJsonArray.size() == 0) {
            return null;
        }
        ArrayList<ShoppingListSummary> shoppingListSummaries;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<ShoppingListSummary>>() {
        }.getType();
        shoppingListSummaries = gson.fromJson(shoppingListSummaryJsonArray, collectionType);
        return shoppingListSummaries;
    }

    private static ShoppingListName parseShoppingListName(JsonObject jsonObject) {
        ShoppingListName shoppingListName = new ShoppingListName();
        shoppingListName.setName(jsonObject.get(Constants.SHOPPING_LIST_NAME).getAsString());
        shoppingListName.setSlug(jsonObject.get(Constants.SHOPPING_LIST_SLUG).getAsString());
        shoppingListName.setSystem(jsonObject.get(Constants.SHOPPING_LIST_IS_SYSTEM).getAsInt() == 1);
        return shoppingListName;
    }

    public static BasketOperationResponse parseBasketOperationResponse(String responseJson) {
        BasketOperationResponse basketOperationResponse = null;
        try {
            JSONObject jsonObject = new JSONObject(responseJson);
            basketOperationResponse = new BasketOperationResponse();
            String status = jsonObject.optString(Constants.STATUS, null);
            basketOperationResponse.setStatus(status);
            if (status != null) {
                if (status.equalsIgnoreCase("OK")) {

                    JSONObject responseObject = jsonObject.optJSONObject(Constants.BASKET_CART_OVER);
                    if (responseObject != null) {
                        JSONObject SKUListObject = responseObject.optJSONObject(Constants.BASKET_CART_INNER);
                        addSkuDetails(SKUListObject, basketOperationResponse);
                        JSONObject CARTListObject = responseObject.optJSONObject(Constants.BASKET_CART_OUTER);
                        addCartDetails(CARTListObject, basketOperationResponse);
                    }
                } else if (status.equalsIgnoreCase("ERROR")) {
                    basketOperationResponse.setErrorMessage(jsonObject.optString(Constants.BASKET_ERROR_MESSAGE));
                    basketOperationResponse.setErrorType(jsonObject.optString(Constants.BASKET_ERROR_TYPE));
                }
            }
        } catch (JSONException ex) {

        }
        return basketOperationResponse;
    }

    private static void addCartDetails(JSONObject object, BasketOperationResponse basketOperationResponse) {
        basketOperationResponse.setTotalPriceCart(object.optString(Constants.BASKET_CART_INNER_TOTAl_PRICE));
        basketOperationResponse.setTotalSale(object.optString(Constants.BASKET_CART_OUTER_TOTAl_SAVE_PRICE));
        basketOperationResponse.setNoOfItems(object.optString(Constants.BASKET_CART_OUTER_CART_COUNT));
    }

    private static void addSkuDetails(JSONObject object, BasketOperationResponse addDeleteItemsToBasket) {
        addDeleteItemsToBasket.setTotalPrice(object.optString(Constants.BASKET_CART_INNER_TOTAl_PRICE));
        addDeleteItemsToBasket.setTotalQuantity(object.optString(Constants.BASKET_CART_INNER_CART_COUNT));
        addDeleteItemsToBasket.setUnitPrice(object.optString(Constants.BASKET_CART_INNER_PRICE));
        addDeleteItemsToBasket.setTotalSale(object.optString(Constants.BASKET_CART_OUTER_TOTAl_SAVE_PRICE));
        addDeleteItemsToBasket.setSlug(object.optString(Constants.BASKET_PRODUCT_SLUG));
    }

    public static CartSummary parseGetCartCountResponse(String responseObj) {
        try {
            JSONObject object = new JSONObject(responseObj);
            return parseGetCartCountResponse(object);
        } catch (JSONException e) {
            return null;
        }
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

    public static ArrayList<Section> parseSectionResponse(JsonArray sectionJsonArray) {
        ArrayList<Section> sectionList;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Section>>() {
        }.getType();
        sectionList = gson.fromJson(sectionJsonArray, collectionType);
        return sectionList;
    }

    public static HashMap<Integer, DestinationInfo> parseDestinationInfo(JsonArray destinationInfoJsonArray) {
        HashMap<Integer, DestinationInfo> destinationInfoHashMap = new HashMap<>();
        ArrayList<DestinationInfo> destinationInfos;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<DestinationInfo>>() {
        }.getType();
        destinationInfos = gson.fromJson(destinationInfoJsonArray, collectionType);
        if (destinationInfos != null && destinationInfos.size() > 0) {
            for (DestinationInfo destinationInfo : destinationInfos) {
                destinationInfoHashMap.put(destinationInfo.getDestinationInfoId(), destinationInfo);
            }
        }
        return destinationInfoHashMap;
    }

    public static OrderInvoice parseOrderInvoice(JsonObject orderInvoiceJsonObj) {
        OrderInvoice orderInvoice;
        Gson gson = new Gson();
        orderInvoice = gson.fromJson(orderInvoiceJsonObj, OrderInvoice.class);
        return orderInvoice;
    }
}
