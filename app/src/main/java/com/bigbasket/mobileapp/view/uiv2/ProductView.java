package com.bigbasket.mobileapp.view.uiv2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.product.ProductListSpinnerAdapter;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.handler.OnBrandPageListener;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.BasketOperationTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListNamesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProductView {

    public static <T> void setProductView(final ProductViewHolder productViewHolder, final Product product, String baseImgUrl,
                                          ProductDetailOnClickListener productDetailOnClickListener,
                                          ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                          final boolean skipChildDropDownRendering,
                                          final T productDataAware, String navigationCtx,
                                          @Nullable HashMap<String, Integer> cartInfo,
                                          String tabName,
                                          HashMap<String, String> appDataStoreAvailabilityMap) {
        setProductImage(productViewHolder, product, baseImgUrl, productDetailOnClickListener);
        setProductDesc(productViewHolder, product, productViewDisplayDataHolder,
                productDetailOnClickListener, productDataAware);
        setPrice(productViewHolder, product, productViewDisplayDataHolder);
        setExpressMsg(productViewHolder, product, productViewDisplayDataHolder, productDataAware,
                navigationCtx, cartInfo, tabName, appDataStoreAvailabilityMap);
        setPromo(productViewHolder, product, productViewDisplayDataHolder, productDataAware);
        setProductAdditionalActionMenu(productViewHolder, product, productViewDisplayDataHolder,
                productDataAware, null);
        setBasketAndAvailabilityViews(productViewHolder, product, productViewDisplayDataHolder,
                productDataAware, navigationCtx, cartInfo, tabName, null,
                product.getStoreAvailability() != null && product.getStoreAvailability().size() > 0
                        ? product.getStoreAvailability().get(0) : null);
        if (!skipChildDropDownRendering) {
            setChildProducts(productViewHolder, product, baseImgUrl, productViewDisplayDataHolder,
                    productDataAware, navigationCtx, cartInfo, tabName, appDataStoreAvailabilityMap);
        }
    }

    private static void setProductImage(ProductViewHolder productViewHolder, Product product, String baseImgUrl,
                                        ProductDetailOnClickListener productDetailOnClickListener) {
        ImageView imgProduct = productViewHolder.getImgProduct();
        if (product.getImageUrl() != null) {
            UIUtil.displayAsyncImage(imgProduct, baseImgUrl != null ? baseImgUrl + product.getImageUrl() :
                    product.getImageUrl());
        } else {
            imgProduct.setImageResource(R.drawable.noimage);
        }
        imgProduct.setOnClickListener(productDetailOnClickListener);
    }

    private static <T> void setChildProducts(final ProductViewHolder productViewHolder, final Product product,
                                             final String baseImgUrl,
                                             final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                             final T productDataAware, final String navigationCtx,
                                             @Nullable HashMap<String, Integer> cartInfo,
                                             @Nullable String tabName, HashMap<String, String> AppDataStoreAvailabilityMap) {
        final List<Product> childProducts = product.getAllProducts();
        boolean hasChildren = childProducts != null && childProducts.size() > 0;
        final Button btnMorePackSizes = productViewHolder.getBtnMorePackSizes();
        btnMorePackSizes.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        TextView txtPackageDesc = productViewHolder.getTxtPackageDesc();
        txtPackageDesc.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        if (hasChildren) {
            btnMorePackSizes.setText(product.getWeightAndPackDesc());
            btnMorePackSizes.setVisibility(View.VISIBLE);
            txtPackageDesc.setVisibility(View.GONE);
            OnShowChildProductDropdownClickListener listener = null;
            Product childProductInBasket = null;
            if (!productViewDisplayDataHolder.disableInBasketChildSwap()) {
                boolean isCurrentProductInBasket = getNoOfItemsInCart(product, cartInfo) > 0;
                if (!isCurrentProductInBasket) {
                    for (Product childProduct : childProducts) {
                        boolean isThisChildProductInBasket = getNoOfItemsInCart(childProduct, cartInfo) > 0;
                        if (isThisChildProductInBasket) {
                            childProductInBasket = childProduct;
                            listener = new OnShowChildProductDropdownClickListener<>(productDataAware, productViewDisplayDataHolder,
                                    product, productViewHolder, baseImgUrl, navigationCtx, cartInfo,
                                    tabName, AppDataStoreAvailabilityMap);
                            listener.setCurrentProduct(childProductInBasket);
                            break;
                        }
                    }
                }
            }
            if (listener == null) {
                listener = new OnShowChildProductDropdownClickListener<>(productDataAware, productViewDisplayDataHolder,
                        product, productViewHolder, baseImgUrl, navigationCtx, cartInfo, tabName,
                        AppDataStoreAvailabilityMap);
            } else {
                // There is a child product that is in basket
                // Manually trigger click to change to that product
                listener.swapWithChildProduct(childProductInBasket, btnMorePackSizes, tabName);
            }
            btnMorePackSizes.setOnClickListener(listener);
        } else {
            btnMorePackSizes.setVisibility(View.GONE);
            txtPackageDesc.setText(product.getWeightAndPackDesc());
            txtPackageDesc.setVisibility(View.VISIBLE);
        }
    }

    private static <T> void setProductDesc(ProductViewHolder productViewHolder, Product product,
                                           ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                           ProductDetailOnClickListener productDetailOnClickListener,
                                           final T productDataAware) {
        TextView txtProductDesc = productViewHolder.getTxtProductDesc();
        TextView txtProductBrand = productViewHolder.getTxtProductBrand();
        txtProductDesc.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        //txtProductDesc.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        if (!TextUtils.isEmpty(product.getDescription())) {
            txtProductDesc.setText(product.getDescription());
            txtProductDesc.setVisibility(View.VISIBLE);
        } else {
            txtProductDesc.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(product.getBrand())) {
            txtProductBrand.setText(product.getBrand());
            txtProductBrand.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(product.getBrandSlug())) {
                txtProductBrand.setOnClickListener(new OnBrandPageListener((LaunchProductListAware) productDataAware,
                        product.getBrandSlug()));
            }
        } else {
            txtProductBrand.setVisibility(View.GONE);
        }
        txtProductDesc.setOnClickListener(productDetailOnClickListener);
    }

    private static void setPrice(ProductViewHolder productViewHolder, Product product,
                                 ProductViewDisplayDataHolder productViewDisplayDataHolder) {
        TextView txtSalePrice = productViewHolder.getTxtSalePrice();
        boolean hasSavings = product.hasSavings();
        txtSalePrice.setTypeface(productViewDisplayDataHolder.getSerifTypeface());

        TextView txtMrp = productViewHolder.getTxtMrp();
        txtMrp.setTypeface(productViewDisplayDataHolder.getSerifTypeface());

        if (hasSavings && !TextUtils.isEmpty(product.getMrp())) {
            String prefix = "`";
            String mrpStr = UIUtil.formatAsMoney(Double.parseDouble(product.getMrp()));
            int prefixLen = prefix.length();
            SpannableString spannableMrp = new SpannableString(prefix + mrpStr);
            spannableMrp.setSpan(new CustomTypefaceSpan("", productViewDisplayDataHolder.getRupeeTypeface()), prefixLen - 1,
                    prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableMrp.setSpan(new StrikethroughSpan(), 0,
                    spannableMrp.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtMrp.setText(spannableMrp);
            txtMrp.setVisibility(View.VISIBLE);
        } else {
            txtMrp.setVisibility(View.GONE);
        }
        txtSalePrice.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        txtSalePrice.setText(UIUtil.asRupeeSpannable(
                UIUtil.formatAsMoney(Double.parseDouble(product.getSellPrice())), productViewDisplayDataHolder.getRupeeTypeface()));
    }

    private static <T> void setExpressMsg(final ProductViewHolder productViewHolder, final Product product,
                                          final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                          final T productDataAware, final String navigationCtx,
                                          @Nullable final HashMap<String, Integer> cartInfo,
                                          final String tabName,
                                          @Nullable final HashMap<String, String> appDataStoreAvailabilityMap) {

        final ArrayList<HashMap<String, String>> storeAvailabilityArrayList = product.getStoreAvailability();
        LinearLayout layoutExpressMsg = productViewHolder.getLayoutExpressMsg();
        RadioGroup radioGroupExpress = productViewHolder.getRadioGroupExpress();
        radioGroupExpress.removeAllViews();
        if (storeAvailabilityArrayList != null && storeAvailabilityArrayList.size() > 0 &&
                appDataStoreAvailabilityMap != null && productViewDisplayDataHolder.isShowBasketBtn()) {
            TextView txtExpressMsg = productViewHolder.getTxtExpressMsg();
            if(AppDataDynamic.getInstance(((ActivityAware) productDataAware).getCurrentActivity()).isContextualMode() &&
                    storeAvailabilityArrayList.size() > 1){
                for(HashMap<String, String> stringStringHashMap : storeAvailabilityArrayList){
                    if(stringStringHashMap.containsKey(Constants.TAB_TYPE) &&
                            stringStringHashMap.get(Constants.TAB_TYPE).equals(Constants.EXPRESS)){
                        setExpressDisplayNameMsg(stringStringHashMap, radioGroupExpress,
                                appDataStoreAvailabilityMap, txtExpressMsg);
                    }
                }
            }else {
                productViewHolder.getLayoutExpressMsg().setVisibility(View.VISIBLE);
                if (storeAvailabilityArrayList.size() > 1) {
                    for (int i = 0; i < storeAvailabilityArrayList.size(); i++) {
                        String availabilityInfoId = storeAvailabilityArrayList.get(i).get(Constants.AVAILABILITY_INFO_ID);
                        if (!TextUtils.isEmpty(availabilityInfoId) && appDataStoreAvailabilityMap.containsKey(availabilityInfoId)) {
                            RadioButton rbtnAvailabilityType = UIUtil.
                                    getPaymentOptionRadioButton(radioGroupExpress,
                                            ((ActivityAware) productDataAware).getCurrentActivity(),
                                            ((ActivityAware) productDataAware).getCurrentActivity().getLayoutInflater());
                            rbtnAvailabilityType.setText(appDataStoreAvailabilityMap.get(availabilityInfoId));
                            rbtnAvailabilityType.setId(i);
                            if (i == 0) {
                                rbtnAvailabilityType.setChecked(true);
                            }
                            radioGroupExpress.addView(rbtnAvailabilityType);
                        }
                    }
                    if (radioGroupExpress.getChildCount() > 1) {
                        radioGroupExpress.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                                setProductAdditionalActionMenu(productViewHolder, product, productViewDisplayDataHolder,
                                        productDataAware, String.valueOf(radioGroup.getFocusedChild().getTag()));
                                setBasketAndAvailabilityViews(productViewHolder, product, productViewDisplayDataHolder,
                                        productDataAware, navigationCtx, cartInfo, tabName,
                                        storeAvailabilityArrayList.get(radioGroup.getFocusedChild().getId()).get(Constants.PRODUCT_STATUS),
                                        storeAvailabilityArrayList.get(radioGroup.getFocusedChild().getId()));
                            }
                        });
                        txtExpressMsg.setVisibility(View.GONE);
                        radioGroupExpress.setVisibility(View.VISIBLE);
                    } else {
                        setExpressDisplayNameMsg(storeAvailabilityArrayList.get(0), radioGroupExpress,
                                appDataStoreAvailabilityMap, txtExpressMsg);
                    }
                } else {
                    setExpressDisplayNameMsg(storeAvailabilityArrayList.get(0), radioGroupExpress,
                            appDataStoreAvailabilityMap, txtExpressMsg);
                }
                layoutExpressMsg.setVisibility(View.VISIBLE);
            }
        } else {
            layoutExpressMsg.setVisibility(View.GONE);
        }
    }

    private static void setExpressDisplayNameMsg(HashMap<String, String> storeAvailabilityHashMap,
                                                 RadioGroup radioGroupExpress,
                                                 HashMap<String, String> storeAvailabilityMap,
                                                 TextView txtExpressMsg) {
        String availabilityInfoId = storeAvailabilityHashMap.get(Constants.AVAILABILITY_INFO_ID);
        radioGroupExpress.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(availabilityInfoId) && storeAvailabilityMap.containsKey(availabilityInfoId)) {
            txtExpressMsg.setText(storeAvailabilityMap.get(availabilityInfoId));
            txtExpressMsg.setVisibility(View.VISIBLE);
        } else {
            txtExpressMsg.setVisibility(View.GONE);
        }
    }

    private static <T> void setPromo(ProductViewHolder productViewHolder, Product product, ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                     final T activityAware) {
        ImageView imgPromoStar = productViewHolder.getImgPromoStar();
        TextView txtPromoDesc = productViewHolder.getTxtPromoDesc();
        TextView txtPromoAddSavings = productViewHolder.getTxtPromoAddSavings();
        if (product.getProductPromoInfo() != null &&
                Promo.getAllTypes().contains(product.getProductPromoInfo().getPromoType())) {
            //Show Promo Saving
            if (product.getProductPromoInfo().getPromoSavings() > 0) {
                String label = product.hasSavings() ? "Save Additional `" : "Save `";
                String promoSavingStr = label +
                        product.getProductPromoInfo().getFormattedPromoSavings();
                SpannableString savingSpannable =
                        new SpannableString(promoSavingStr);
                int labelLength = label.length();
                savingSpannable.setSpan(new
                                CustomTypefaceSpan("", productViewDisplayDataHolder.getRupeeTypeface()),
                        labelLength - 1,
                        labelLength, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                );
                txtPromoAddSavings.setVisibility(View.VISIBLE);
                txtPromoAddSavings.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
                txtPromoAddSavings.setText(savingSpannable);
            } else {
                txtPromoAddSavings.setVisibility(View.GONE);
            }

            //promo start image
            imgPromoStar.setVisibility(View.VISIBLE);

            //Show Promo Name
            String promoDesc = product.getProductPromoInfo().getPromoDesc();
            txtPromoDesc.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
            txtPromoDesc.setVisibility(View.VISIBLE);
            txtPromoDesc.setText(promoDesc);
            final int promoId = product.getProductPromoInfo().getId();
            View.OnClickListener promoOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent promoDetailIntent = new Intent(((ActivityAware) activityAware).getCurrentActivity(), BackButtonActivity.class);
                    promoDetailIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
                    promoDetailIntent.putExtra(Constants.PROMO_ID, promoId);
                    ((ActivityAware) activityAware).getCurrentActivity().startActivityForResult(promoDetailIntent, NavigationCodes.GO_TO_HOME);
                }
            };
            txtPromoDesc.setOnClickListener(promoOnClickListener);
            imgPromoStar.setOnClickListener(promoOnClickListener);
        } else {
            imgPromoStar.setVisibility(View.GONE);
            txtPromoDesc.setVisibility(View.GONE);
            txtPromoAddSavings.setVisibility(View.GONE);
        }
    }

    private static String getAvailability(Product product, String storeAvailability) {
        if (!TextUtils.isEmpty(storeAvailability)) {
            return storeAvailability;
        } else if (product.getStoreAvailability() != null && product.getStoreAvailability().size() > 0 &&
                product.getStoreAvailability().get(0).containsKey(Constants.PRODUCT_STATUS)) {
            return product.getStoreAvailability().get(0).get(Constants.PRODUCT_STATUS);
        } else {
            return product.getProductStatus();
        }
    }

    private static <T> void setProductAdditionalActionMenu(ProductViewHolder productViewHolder, final Product product,
                                                           final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                           final T shoppingListNamesAware, String storeAvailability) {
        final ImageView imgProductOverflowAction = productViewHolder.getImgProductOverflowAction();
        if ((productViewDisplayDataHolder.isShowShoppingListBtn() || productViewDisplayDataHolder.showShopListDeleteBtn())
                && productViewDisplayDataHolder.isLoggedInMember()
                && !getAvailability(product, storeAvailability).equalsIgnoreCase("N")) {
            int imageDrawableId = productViewDisplayDataHolder.showShopListDeleteBtn() ?
                    R.drawable.delete_product : R.drawable.add_to_shopping_list;
            if (productViewDisplayDataHolder.showShopListDeleteBtn()) {
                imgProductOverflowAction.setImageDrawable(
                        ContextCompat.getDrawable(((ActivityAware) shoppingListNamesAware).getCurrentActivity(), imageDrawableId));
            }
            imgProductOverflowAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (productViewDisplayDataHolder.showShopListDeleteBtn()) {
                        android.support.v7.app.AlertDialog.Builder builder =
                                new android.support.v7.app.AlertDialog.Builder(((ActivityAware) shoppingListNamesAware).getCurrentActivity());
                        builder.setTitle(R.string.app_name)
                                .setMessage(R.string.deleteProductFromShoppingList)
                                .setCancelable(false)
                                .setPositiveButton(R.string.yesTxt, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (((ConnectivityAware) shoppingListNamesAware).checkInternetConnection()) {
                                            List<ShoppingListName> shoppingListNames = new ArrayList<>();
                                            shoppingListNames.add(productViewDisplayDataHolder.getShoppingListName());
                                            ShoppingListDoAddDeleteTask shoppingListDoAddDeleteTask =
                                                    new ShoppingListDoAddDeleteTask<>(shoppingListNamesAware, shoppingListNames, ShoppingListOption.DELETE_ITEM);
                                            ((ShoppingListNamesAware) shoppingListNamesAware).setSelectedProductId(product.getSku());
                                            shoppingListDoAddDeleteTask.startTask();
                                        } else {
                                            productViewDisplayDataHolder.getHandler().sendOfflineError();
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.noTxt, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        android.support.v7.app.AlertDialog alertDialog = builder.create();
                        alertDialog.setOnShowListener(new OnDialogShowListener());
                        alertDialog.show();
                    } else {
                        if (((ConnectivityAware) shoppingListNamesAware).checkInternetConnection()) {
                            ((TrackingAware) (shoppingListNamesAware)).trackEvent(TrackingAware.ADD_TO_SHOPPING_LIST, null);
                            ((ShoppingListNamesAware) shoppingListNamesAware).setSelectedProductId(product.getSku());
                            new ShoppingListNamesTask<>(shoppingListNamesAware, false).startTask();
                        } else {
                            productViewDisplayDataHolder.getHandler().sendOfflineError();
                        }
                    }
                }
            });
        } else {
            imgProductOverflowAction.setVisibility(View.GONE);
        }
    }

    private static int getNoOfItemsInCart(Product product, @Nullable final HashMap<String, Integer> cartInfo) {
        int noOfItemsInCart;
        if (cartInfo != null) {
            noOfItemsInCart = cartInfo.containsKey(product.getSku()) ?
                    cartInfo.get(product.getSku()) : 0;
        } else {
            noOfItemsInCart = product.getNoOfItemsInCart();
        }
        return noOfItemsInCart;
    }

    private static <T> void setBasketAndAvailabilityViews(final ProductViewHolder productViewHolder, final Product product,
                                                          final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                          final T basketOperationAware, final String navigationCtx,
                                                          @Nullable final HashMap<String, Integer> cartInfo,
                                                          final String tabName, String storeAvailability,
                                                          @Nullable HashMap<String, String> productStoreAvailabilityMap) {
        final ImageView imgAddToBasket = productViewHolder.getImgAddToBasket();
        final View viewDecBasketQty = productViewHolder.getViewDecBasketQty();
        final TextView txtInBasket = productViewHolder.getTxtInBasket();
        final View viewIncBasketQty = productViewHolder.getViewIncBasketQty();
        final EditText editTextQty = productViewHolder.getEditTextQty();

        TextView txtOutOfStockORNotForSale = productViewHolder.getTxtOutOfStockORNotForSale();
        txtInBasket.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
        editTextQty.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
        editTextQty.setText("1");

        ArrayList<String> addToBasketPostParamsArrayList = AppDataDynamic.getInstance(((ActivityAware) basketOperationAware)
                .getCurrentActivity()).getAddToBasketPostParams();
        final Map<String, String> basketQueryMap = new HashMap<>();
        if (addToBasketPostParamsArrayList != null && addToBasketPostParamsArrayList.size() > 0
                && productStoreAvailabilityMap != null && productStoreAvailabilityMap.size() > 0) {
            for (String basketPostParam : addToBasketPostParamsArrayList) {
                if (productStoreAvailabilityMap.containsKey(basketPostParam)) {
                    basketQueryMap.put(basketPostParam, productStoreAvailabilityMap.get(basketPostParam));
                }
            }
        }

        if (productViewDisplayDataHolder.isShowBasketBtn()) {
            if (getAvailability(product, storeAvailability).equalsIgnoreCase("A")) {
                int noOfItemsInCart = getNoOfItemsInCart(product, cartInfo);

                if (noOfItemsInCart > 0) {
                    txtInBasket.setText(String.valueOf(noOfItemsInCart));
                    txtInBasket.setVisibility(View.VISIBLE);
                    viewDecBasketQty.setVisibility(View.VISIBLE);
                    viewIncBasketQty.setVisibility(View.VISIBLE);
                    //productViewHolder.itemView.setBackgroundColor(Constants.IN_BASKET_COLOR);
                    if (productViewDisplayDataHolder.isShowQtyInput()) {
                        editTextQty.setVisibility(View.GONE);
                    }
                    imgAddToBasket.setVisibility(View.GONE);
                } else {
                    txtInBasket.setText("");
                    txtInBasket.setVisibility(View.GONE);
                    viewDecBasketQty.setVisibility(View.GONE);
                    viewIncBasketQty.setVisibility(View.GONE);

                    imgAddToBasket.setVisibility(View.VISIBLE);
                    if (productViewDisplayDataHolder.isShowQtyInput()) {
                        editTextQty.setVisibility(View.VISIBLE);
                    }
                    //productViewHolder.itemView.setBackgroundColor(Color.WHITE);
                }

                viewIncBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((ConnectivityAware) basketOperationAware).checkInternetConnection()) {
                            BasketOperationTask<T> basketOperationTask = new BasketOperationTask<>(basketOperationAware,
                                    BasketOperation.INC, product,
                                    txtInBasket, viewDecBasketQty, viewIncBasketQty, imgAddToBasket,
                                    TrackingAware.BASKET_INCREMENT, navigationCtx, productViewHolder.itemView, cartInfo,
                                    editTextQty, tabName, basketQueryMap);
                            basketOperationTask.startTask();

                        } else {
                            productViewDisplayDataHolder.getHandler().sendOfflineError();
                        }
                    }
                });

                viewDecBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((ConnectivityAware) basketOperationAware).checkInternetConnection()) {
                            BasketOperationTask<T> myTask = new BasketOperationTask<>(basketOperationAware,
                                    BasketOperation.DEC,
                                    product, txtInBasket, viewDecBasketQty, viewIncBasketQty,
                                    imgAddToBasket, TrackingAware.BASKET_DECREMENT,
                                    navigationCtx, productViewHolder.itemView, cartInfo, editTextQty,
                                    tabName, basketQueryMap);
                            myTask.startTask();
                        } else {
                            productViewDisplayDataHolder.getHandler().sendOfflineError();
                        }
                    }

                });

                imgAddToBasket.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((ConnectivityAware) basketOperationAware).checkInternetConnection()) {
                            String qty = "1";
                            if (productViewDisplayDataHolder.isShowQtyInput()) {
                                String txt = editTextQty.getText().toString();
                                if (TextUtils.isEmpty(txt)) {
                                    Toast.makeText(((ActivityAware) basketOperationAware).getCurrentActivity(), "Quantity shouldn\'t be blank", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                BaseActivity.hideKeyboard(((ActivityAware) basketOperationAware).getCurrentActivity(),
                                        editTextQty);
                                qty = txt;
                            }
                            BasketOperationTask<T> basketOperationTask = new BasketOperationTask<>(basketOperationAware,
                                    BasketOperation.INC, product,
                                    txtInBasket, viewDecBasketQty, viewIncBasketQty, imgAddToBasket,
                                    qty, TrackingAware.BASKET_ADD, navigationCtx, productViewHolder.itemView,
                                    cartInfo, editTextQty, tabName, basketQueryMap);
                            basketOperationTask.startTask();
                        } else {
                            productViewDisplayDataHolder.getHandler().sendOfflineError();
                        }
                    }
                });
                txtOutOfStockORNotForSale.setVisibility(View.GONE);
            } else {
                txtInBasket.setVisibility(View.GONE);
                viewDecBasketQty.setVisibility(View.GONE);
                viewIncBasketQty.setVisibility(View.GONE);
                editTextQty.setVisibility(View.GONE);
                imgAddToBasket.setVisibility(View.GONE);

                txtOutOfStockORNotForSale.setVisibility(View.VISIBLE);
                txtOutOfStockORNotForSale.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
                if (getAvailability(product, storeAvailability).equalsIgnoreCase("0")
                        || getAvailability(product, storeAvailability).equalsIgnoreCase("O")) {  // zero not O
                    txtOutOfStockORNotForSale.setText("Out of Stock");
                } else {
                    txtOutOfStockORNotForSale.setText("Not for sale");
                }
                //productViewHolder.itemView.setBackgroundColor(Color.WHITE);
            }
        } else {
            txtInBasket.setVisibility(View.GONE);
            viewDecBasketQty.setVisibility(View.GONE);
            viewIncBasketQty.setVisibility(View.GONE);
            imgAddToBasket.setVisibility(View.GONE);
            editTextQty.setVisibility(View.GONE);
            //productViewHolder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    public static class OnShowChildProductDropdownClickListener<T> implements View.OnClickListener {

        private T productDataAware;
        private ProductViewDisplayDataHolder productViewDisplayDataHolder;
        private Product product;
        private List<Product> childProducts;
        private ProductViewHolder productViewHolder;
        private String baseImgUrl;
        private String navigationCtx;
        private Product currentProduct;
        private HashMap<String, Integer> cartInfo;
        private String tabName;
        private HashMap<String, String> AppDataStoreAvailabilityMap;

        public OnShowChildProductDropdownClickListener(T productDataAware, ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                       Product product, ProductViewHolder productViewHolder,
                                                       String baseImgUrl, String navigationCtx,
                                                       @Nullable HashMap<String, Integer> cartInfo,
                                                       String tabName, HashMap<String, String> AppDataStoreAvailabilityMap) {
            this.productDataAware = productDataAware;
            this.productViewDisplayDataHolder = productViewDisplayDataHolder;
            this.product = product;
            this.currentProduct = product;
            this.productViewHolder = productViewHolder;
            this.baseImgUrl = baseImgUrl;
            this.navigationCtx = navigationCtx;
            this.childProducts = product.getAllProducts();
            this.cartInfo = cartInfo;
            this.tabName = tabName;
            this.AppDataStoreAvailabilityMap = AppDataStoreAvailabilityMap;
        }

        @Override
        public void onClick(View v) {
            final Button btnMorePackSizes = (Button) v;
            AlertDialog.Builder builder = new AlertDialog.Builder(((ActivityAware) productDataAware).getCurrentActivity());
            final AlertDialog dialog = builder.create();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View childDropdown = ((ActivityAware) productDataAware).getCurrentActivity().getLayoutInflater()
                    .inflate(R.layout.uiv3_list_dialog, null);
            ListView listView = (ListView) childDropdown.findViewById(R.id.lstDialog);

            TextView txtChildDropdownTitle = (TextView) childDropdown.findViewById(R.id.txtListDialogTitle);
            txtChildDropdownTitle.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
            txtChildDropdownTitle.setText("Select Pack Size");

            final ProductListSpinnerAdapter productListSpinnerAdapter = new ProductListSpinnerAdapter(((ActivityAware) productDataAware).getCurrentActivity(),
                    childProducts, productViewDisplayDataHolder.getSerifTypeface(),
                    productViewDisplayDataHolder.getRupeeTypeface(), product, baseImgUrl);
            productListSpinnerAdapter.setCurrentProduct(currentProduct);
            listView.setAdapter(productListSpinnerAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Product childProduct = childProducts.get(position);
                    swapWithChildProduct(childProduct, btnMorePackSizes, tabName);
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }

                }
            });
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setView(childDropdown, 0, 0, 0, 0);
            dialog.show();
        }

        public void swapWithChildProduct(Product childProduct, Button btnMorePackSizes, String tabName) {
            if (childProduct.getSku().equals(product.getSku())) {
                childProduct = product;
            }
            btnMorePackSizes.setText(childProduct.getWeightAndPackDesc());
            currentProduct = childProduct;
            setProductView(productViewHolder, childProduct, baseImgUrl,
                    new ProductDetailOnClickListener(childProduct.getSku(), (ActivityAware) productDataAware),
                    productViewDisplayDataHolder, true, productDataAware, navigationCtx, cartInfo,
                    tabName, AppDataStoreAvailabilityMap);
        }

        public void setCurrentProduct(Product currentProduct) {
            this.currentProduct = currentProduct;
        }
    }
}
