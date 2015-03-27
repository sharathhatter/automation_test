package com.bigbasket.mobileapp.view.uiv2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.product.ProductListSpinnerAdapter;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
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
import java.util.List;

public final class ProductView {

    public static <T> void setProductView(final ProductViewHolder productViewHolder, final Product product, String baseImgUrl,
                                          ProductDetailOnClickListener productDetailOnClickListener,
                                          ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                          final boolean skipChildDropDownRendering,
                                          final T productDataAware, String navigationCtx) {
        setProductImage(productViewHolder, product, baseImgUrl, productDetailOnClickListener);
        setIsNewAndBby(productViewHolder, product);
        if (!skipChildDropDownRendering) {
            setChildProducts(productViewHolder, product, baseImgUrl, productViewDisplayDataHolder,
                    productDataAware, navigationCtx);
        }
        setProductBrand(productViewHolder, product, productViewDisplayDataHolder, productDetailOnClickListener);
        setProductDesc(productViewHolder, product, productViewDisplayDataHolder, productDetailOnClickListener);
        setPrice(productViewHolder, product, productViewDisplayDataHolder);
        setPromo(productViewHolder, product, productViewDisplayDataHolder, productDataAware);
        setProductAdditionalActionMenu(productViewHolder, product, productViewDisplayDataHolder, productDataAware);
        setBasketAndAvailabilityViews(productViewHolder, product, productViewDisplayDataHolder,
                productDataAware, navigationCtx);
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

    private static void setIsNewAndBby(ProductViewHolder productViewHolder, Product product) {
        ImageView imgBby = productViewHolder.getImgBby();
        if (product.isBbyProduct()) {
            imgBby.setVisibility(View.VISIBLE);
        } else {
            imgBby.setVisibility(View.GONE);
            TextView txtIsNewProduct = productViewHolder.getTxtIsNewProduct();
            txtIsNewProduct.setVisibility(product.isNewProduct() ? View.VISIBLE : View.GONE);
        }
    }

    private static <T> void setChildProducts(final ProductViewHolder productViewHolder, Product product,
                                             final String baseImgUrl,
                                             final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                             final T productDataAware, final String navigationCtx) {
        final List<Product> childProducts = product.getAllProducts();
        boolean hasChildren = childProducts != null && childProducts.size() > 0;
        Spinner spinnerPackageDesc = productViewHolder.getSpinnerPackageDesc();
        TextView packageDescTxtView = productViewHolder.getPackageDescTextView();
        if (hasChildren) {
            ProductListSpinnerAdapter productListSpinnerAdapter = new ProductListSpinnerAdapter(((ActivityAware) productDataAware).getCurrentActivity(), android.R.layout.simple_spinner_item,
                    childProducts, productViewDisplayDataHolder.getSansSerifMediumTypeface(),
                    productViewDisplayDataHolder.getRupeeTypeface());
            spinnerPackageDesc.setAdapter(productListSpinnerAdapter);
            productListSpinnerAdapter.notifyDataSetChanged();
            spinnerPackageDesc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Product childProduct = childProducts.get(position);
                    setProductView(productViewHolder, childProduct, baseImgUrl,
                            new ProductDetailOnClickListener(childProduct.getSku(), (ActivityAware) productDataAware),
                            productViewDisplayDataHolder, true, productDataAware, navigationCtx);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinnerPackageDesc.setVisibility(View.VISIBLE);
            packageDescTxtView.setVisibility(View.GONE);
        } else {
            packageDescTxtView.setText(product.getWeightAndPackDesc());
            packageDescTxtView.setVisibility(View.VISIBLE);
            packageDescTxtView.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
            spinnerPackageDesc.setVisibility(View.GONE);
        }
    }

    private static void setProductBrand(ProductViewHolder productViewHolder, Product product, ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                        ProductDetailOnClickListener productDetailOnClickListener) {
        TextView txtProductBrand = productViewHolder.getTxtProductBrand();
        txtProductBrand.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
        if (!TextUtils.isEmpty(product.getBrand())) {
            txtProductBrand.setText(product.getBrand());
        } else {
            txtProductBrand.setVisibility(View.GONE);
        }
        txtProductBrand.setOnClickListener(productDetailOnClickListener);
    }

    private static void setProductDesc(ProductViewHolder productViewHolder, Product product, ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                       ProductDetailOnClickListener productDetailOnClickListener) {
        TextView txtProductDesc = productViewHolder.getTxtProductDesc();
        txtProductDesc.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        if (!TextUtils.isEmpty(product.getDescription())) {
            txtProductDesc.setText(product.getDescription());
            txtProductDesc.setVisibility(View.VISIBLE);
        } else {
            txtProductDesc.setVisibility(View.GONE);
        }
        txtProductDesc.setOnClickListener(productDetailOnClickListener);
    }

    private static void setPrice(ProductViewHolder productViewHolder, Product product,
                                 ProductViewDisplayDataHolder productViewDisplayDataHolder) {
        TextView labelMrp = productViewHolder.getLabelMrp();
        TextView txtSalePrice = productViewHolder.getTxtSalePrice();
        boolean hasSavings = product.hasSavings();
        labelMrp.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
        txtSalePrice.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());

        TextView txtMrp = productViewHolder.getTxtMrp();
        txtMrp.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());

        if (hasSavings && !TextUtils.isEmpty(product.getMrp())) {
            String prefix = " `";
            String mrpStr = product.getMrp() + " ";
            int prefixLen = prefix.length();
            SpannableString spannableMrp = new SpannableString(prefix + mrpStr);
            spannableMrp.setSpan(new CustomTypefaceSpan("", productViewDisplayDataHolder.getRupeeTypeface()), prefixLen - 1,
                    prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtMrp.setText(spannableMrp);
            labelMrp.setVisibility(View.VISIBLE);
            txtMrp.setVisibility(View.VISIBLE);
        } else {
            txtMrp.setVisibility(View.GONE);
            labelMrp.setVisibility(View.GONE);
        }
        double actualDiscount = product.getActualDiscount();
        TextView txtSave = productViewHolder.getTxtSave();

        if (hasSavings) {
            String prefix = "SAVE: `";
            Spannable spannableSaving = new SpannableString(prefix + String.format("%.2f", actualDiscount));
            spannableSaving.setSpan(new CustomTypefaceSpan("", productViewDisplayDataHolder.getRupeeTypeface()),
                    prefix.length() - 1,
                    prefix.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableSaving.setSpan(new StyleSpan(Typeface.BOLD), 0, prefix.length() - 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtSave.setText(spannableSaving);

            // for line over Mrp text
            txtMrp.setPaintFlags(txtMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            txtSave.setVisibility(View.VISIBLE);
        } else {
            txtSave.setVisibility(View.GONE);
        }
        txtSalePrice.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        txtSalePrice.setText(UIUtil.asRupeeSpannable(product.getSellPrice(), productViewDisplayDataHolder.getRupeeTypeface()));
    }

    private static <T> void setPromo(ProductViewHolder productViewHolder, Product product, ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                     final T activityAware) {
        TextView txtPromoLabel = productViewHolder.getTxtPromoLabel();
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
                txtPromoAddSavings.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
                txtPromoAddSavings.setText(savingSpannable);
            } else {
                txtPromoAddSavings.setVisibility(View.GONE);
            }

            //promo label
            String promoLabel = product.getProductPromoInfo().getPromoLabel();
            if (!TextUtils.isEmpty(promoLabel)) {
                txtPromoLabel.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
                txtPromoLabel.setVisibility(View.VISIBLE);
                txtPromoLabel.setText(promoLabel);
            }

            //Show Promo Name
            String promoDesc = product.getProductPromoInfo().getPromoDesc();
            txtPromoDesc.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
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
            txtPromoLabel.setOnClickListener(promoOnClickListener);
        } else {
            txtPromoLabel.setVisibility(View.GONE);
            txtPromoDesc.setVisibility(View.GONE);
            txtPromoAddSavings.setVisibility(View.GONE);
        }
    }

    private static <T> void setProductAdditionalActionMenu(ProductViewHolder productViewHolder, final Product product,
                                                           final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                           final T shoppingListNamesAware) {
        final ImageView imgAddToShoppingList = productViewHolder.getImgAddToShoppingList();
        setShoppingDeleteButton(productViewHolder, product, productViewDisplayDataHolder, shoppingListNamesAware);
        if (productViewDisplayDataHolder.isShowShoppingListBtn() && productViewDisplayDataHolder.isLoggedInMember()
                && !product.getProductStatus().equalsIgnoreCase("N")) {
            imgAddToShoppingList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((ConnectivityAware) shoppingListNamesAware).checkInternetConnection()) {
                        ((TrackingAware) (shoppingListNamesAware)).trackEvent(TrackingAware.ADD_TO_SHOPPING_LIST, null);
                        ((ShoppingListNamesAware) shoppingListNamesAware).setSelectedProductId(product.getSku());
                        new ShoppingListNamesTask<>(shoppingListNamesAware, false).startTask();
                    } else {
                        productViewDisplayDataHolder.getHandler().sendOfflineError();
                    }
                }
            });
        } else {
            imgAddToShoppingList.setVisibility(View.GONE);
        }
    }

    private static <T> void setShoppingDeleteButton(ProductViewHolder productViewHolder, final Product product,
                                                    final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                    final T shoppingListNamesAware) {

        // for logged in user display add to list icon
        final ImageView imgShoppingListDel = productViewHolder.getImgShoppingListDel();

        if (productViewDisplayDataHolder.showShopListDeleteBtn()) {
            imgShoppingListDel.setVisibility(View.VISIBLE);
            imgShoppingListDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtil.getMaterialDialogBuilder(((ActivityAware) shoppingListNamesAware).getCurrentActivity())
                            .title(R.string.app_name)
                            .content("Are you sure you want to delete this product from the shopping list?")
                            .cancelable(false)
                            .positiveText(R.string.yesTxt)
                            .negativeText(R.string.noTxt)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
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
                            .show();
                }
            });
        } else {
            imgShoppingListDel.setVisibility(View.GONE);
        }
    }

    private static <T> void setBasketAndAvailabilityViews(final ProductViewHolder productViewHolder, final Product product,
                                                          final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                          final T basketOperationAware, final String navigationCtx) {
        final Button btnAddToBasket = productViewHolder.getBtnAddToBasket();
        final TextView txtDecBasketQty = productViewHolder.getTxtDecBasketQty();
        final TextView txtInBasket = productViewHolder.getTxtInBasket();
        final TextView txtIncBasketQty = productViewHolder.getTxtIncBasketQty();
        final EditText editTextQty = productViewHolder.getEditTextQty();

        TextView txtOutOfStockORNotForSale = productViewHolder.getTxtOutOfStockORNotForSale();

        if (productViewDisplayDataHolder.isShowBasketBtn()) {
            if (product.getProductStatus().equalsIgnoreCase("A")) {
                int noOfItemsInCart = product.getNoOfItemsInCart();

                if (noOfItemsInCart > 0) {
                    txtInBasket.setText(noOfItemsInCart + " in");
                    txtInBasket.setVisibility(View.VISIBLE);
                    txtDecBasketQty.setVisibility(View.VISIBLE);
                    txtIncBasketQty.setVisibility(View.VISIBLE);
                    productViewHolder.itemView.setBackgroundColor(Constants.IN_BASKET_COLOR);

                    btnAddToBasket.setVisibility(View.GONE);
                    editTextQty.setVisibility(View.GONE);
                } else {
                    txtInBasket.setText("");
                    txtInBasket.setVisibility(View.GONE);
                    txtDecBasketQty.setVisibility(View.GONE);
                    txtIncBasketQty.setVisibility(View.GONE);

                    btnAddToBasket.setVisibility(View.VISIBLE);
                    editTextQty.setVisibility(View.VISIBLE);
                    productViewHolder.itemView.setBackgroundColor(Color.WHITE);
                }

                txtIncBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(editTextQty.getText())) return;
                        if (((ConnectivityAware) basketOperationAware).checkInternetConnection()) {
                            BasketOperationTask<T> basketOperationTask = new BasketOperationTask<>(basketOperationAware,
                                    BasketOperation.INC, product,
                                    txtInBasket, txtDecBasketQty, txtIncBasketQty, btnAddToBasket,
                                    editTextQty, TrackingAware.BASKET_INCREMENT, navigationCtx, productViewHolder.itemView);
                            basketOperationTask.startTask();

                        } else {
                            productViewDisplayDataHolder.getHandler().sendOfflineError();
                        }
                    }
                });

                txtDecBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(editTextQty.getText())) return;
                        if (((ConnectivityAware) basketOperationAware).checkInternetConnection()) {
                            BasketOperationTask<T> myTask = new BasketOperationTask<>(basketOperationAware,
                                    BasketOperation.DEC,
                                    product, txtInBasket, txtDecBasketQty, txtIncBasketQty,
                                    btnAddToBasket, editTextQty, TrackingAware.BASKET_DECREMENT,
                                    navigationCtx, productViewHolder.itemView);
                            myTask.startTask();
                        } else {
                            productViewDisplayDataHolder.getHandler().sendOfflineError();
                        }
                    }

                });

                btnAddToBasket.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(editTextQty.getText())) return;
                        if (((ConnectivityAware) basketOperationAware).checkInternetConnection()) {
                            String qty = editTextQty.getText() != null ? editTextQty.getText().toString() : "1";
                            BasketOperationTask<T> basketOperationTask = new BasketOperationTask<>(basketOperationAware,
                                    BasketOperation.INC, product,
                                    txtInBasket, txtDecBasketQty, txtIncBasketQty, btnAddToBasket,
                                    editTextQty, qty, TrackingAware.BASKET_ADD, navigationCtx, productViewHolder.itemView);
                            basketOperationTask.startTask();
                        } else {
                            productViewDisplayDataHolder.getHandler().sendOfflineError();
                        }
                    }
                });
                txtOutOfStockORNotForSale.setVisibility(View.GONE);
            } else {
                txtInBasket.setVisibility(View.GONE);
                txtDecBasketQty.setVisibility(View.GONE);
                txtIncBasketQty.setVisibility(View.GONE);
                editTextQty.setVisibility(View.GONE);

                btnAddToBasket.setVisibility(View.GONE);
                txtOutOfStockORNotForSale.setVisibility(View.VISIBLE);
                txtOutOfStockORNotForSale.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
                if (product.getProductStatus().equalsIgnoreCase("0") || product.getProductStatus().equalsIgnoreCase("O")) {  // zero not O
                    txtOutOfStockORNotForSale.setText("Out of Stock");
                } else {
                    txtOutOfStockORNotForSale.setText("Not for sale");
                }
                productViewHolder.itemView.setBackgroundColor(Color.WHITE);
            }
        } else {
            txtInBasket.setVisibility(View.GONE);
            txtDecBasketQty.setVisibility(View.GONE);
            txtIncBasketQty.setVisibility(View.GONE);
            btnAddToBasket.setVisibility(View.GONE);
            editTextQty.setVisibility(View.GONE);
            productViewHolder.itemView.setBackgroundColor(Color.WHITE);
        }
    }
}
