package com.bigbasket.mobileapp.view.uiv2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.product.ProductListSpinnerAdapter;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.BasketOperationTask;
import com.bigbasket.mobileapp.task.ShoppingListDoOperationTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListNamesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public final class ProductView {

    public static View getProductView(final View row, final Product product, String baseImgUrl,
                                      ProductDetailOnClickListener productDetailOnClickListener,
                                      ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                      final BaseActivity context, final boolean skipChildDropDownRendering,
                                      final BaseFragment baseFragment) {
        setProductImage(row, product, baseImgUrl, productDetailOnClickListener);
        setIsNewAndBby(row, product);
        if (!skipChildDropDownRendering) {
            setChildProducts(row, product, context, baseImgUrl, productViewDisplayDataHolder, baseFragment);
        }
        setProductBrand(row, product, productViewDisplayDataHolder, productDetailOnClickListener);
        setProductDesc(row, product, productViewDisplayDataHolder, productDetailOnClickListener);
        setPrice(row, product, productViewDisplayDataHolder, context);
        setPromo(row, product, productViewDisplayDataHolder, context);
        setProductAdditionalActionMenu(row, product, productViewDisplayDataHolder, context, baseFragment);
        setBasketAndAvailabilityViews(row, product, productViewDisplayDataHolder, context, baseFragment);
        return row;
    }

    private static void setProductImage(View row, Product product, String baseImgUrl,
                                        ProductDetailOnClickListener productDetailOnClickListener) {
        ImageView imgProduct = (ImageView) row.findViewById(R.id.imgProduct);
        if (product.getImageUrl() != null) {
            ImageLoader.getInstance().displayImage(baseImgUrl != null ? baseImgUrl + product.getImageUrl() :
                    product.getImageUrl(), imgProduct);
        } else {
            imgProduct.setImageResource(R.drawable.noimage);
        }
        imgProduct.setOnClickListener(productDetailOnClickListener);
    }

    private static void setIsNewAndBby(View row, Product product) {
        ImageView imgBby = (ImageView) row.findViewById(R.id.imgBBY);
        if (product.isBbyProduct()) {
            imgBby.setVisibility(View.VISIBLE);
        } else {
            imgBby.setVisibility(View.GONE);
            TextView txtIsNewProduct = (TextView) row.findViewById(R.id.imgNew);
            txtIsNewProduct.setVisibility(product.isNewProduct() ? View.VISIBLE : View.GONE);
        }
    }

    private static void setChildProducts(final View row, Product product, final BaseActivity context,
                                         final String baseImgUrl,
                                         final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                         final BaseFragment fragment) {
        final List<Product> childProducts = product.getAllProducts();
        boolean hasChildren = childProducts != null && childProducts.size() > 0;
        Spinner spinnerPackageDesc = (Spinner) row.findViewById(R.id.spinnerPackageDesc);
        TextView packageDescTxtView = (TextView) row.findViewById(R.id.txtPackageDesc);
        if (hasChildren) {
            ProductListSpinnerAdapter productListSpinnerAdapter = new ProductListSpinnerAdapter(context, android.R.layout.simple_spinner_item,
                    childProducts, productViewDisplayDataHolder.getSansSerifMediumTypeface(),
                    productViewDisplayDataHolder.getRupeeTypeface());
            spinnerPackageDesc.setAdapter(productListSpinnerAdapter);
            productListSpinnerAdapter.notifyDataSetChanged();
            spinnerPackageDesc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Product childProduct = childProducts.get(position);
                    getProductView(row, childProduct, baseImgUrl,
                            new ProductDetailOnClickListener(childProduct.getSku(), fragment),
                            productViewDisplayDataHolder, context, true, fragment);
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

    private static void setProductBrand(View row, Product product, ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                        ProductDetailOnClickListener productDetailOnClickListener) {
        TextView txtProductBrand = (TextView) row.findViewById(R.id.txtProductBrand);
        txtProductBrand.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
        if (!TextUtils.isEmpty(product.getBrand())) {
            txtProductBrand.setText(product.getBrand());
        } else {
            txtProductBrand.setVisibility(View.GONE);
        }
        txtProductBrand.setOnClickListener(productDetailOnClickListener);
    }

    private static void setProductDesc(View row, Product product, ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                       ProductDetailOnClickListener productDetailOnClickListener) {
        TextView txtProductDesc = (TextView) row.findViewById(R.id.txtProductDesc);
        txtProductDesc.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        if (!TextUtils.isEmpty(product.getDescription())) {
            txtProductDesc.setText(product.getDescription());
            txtProductDesc.setVisibility(View.VISIBLE);
        } else {
            txtProductDesc.setVisibility(View.GONE);
        }
        txtProductDesc.setOnClickListener(productDetailOnClickListener);
    }

    private static void setPrice(View row, Product product,
                                 ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                 BaseActivity context) {
        TextView labelMrp = (TextView) row.findViewById(R.id.labelMrp);
        TextView txtSalePrice = (TextView) row.findViewById(R.id.txtSalePrice);
        boolean hasSavings = product.hasSavings();
        labelMrp.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());
        txtSalePrice.setTypeface(productViewDisplayDataHolder.getSansSerifMediumTypeface());

        TextView txtMrp = (TextView) row.findViewById(R.id.txtMrp);
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
        TextView txtSave = (TextView) row.findViewById(R.id.txtSave);
        ImageView valueStarForSaveTxt = (ImageView) row.findViewById(R.id.valueStarForSaveTxt);

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
            valueStarForSaveTxt.setVisibility(View.VISIBLE);
        } else {
            txtSave.setVisibility(View.GONE);
            valueStarForSaveTxt.setVisibility(View.GONE);
        }
        txtSalePrice.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
        txtSalePrice.setText(context.asRupeeSpannable(product.getSellPrice()));
    }

    private static void setPromo(View row, Product product, ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                 final BaseActivity context) {
        TextView txtPromoLabel = (TextView) row.findViewById(R.id.promoLabel);
        TextView txtPromoDesc = (TextView) row.findViewById(R.id.txtPromoName);
        TextView txtPromoAddSavings = (TextView) row.findViewById(R.id.txtPromoAddSavings);
        if (product.getProductPromoInfo() != null &&
                ArrayUtils.contains(Promo.getAllTypes(), product.getProductPromoInfo().getPromoType())) {
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
//                    Intent promoDetailIntent = new Intent(context,
//                            PromoDetailActivity.class);
//                    promoDetailIntent.putExtra(Constants.PROMO_ID, promoId);
//                    context.startActivityForResult(promoDetailIntent, Constants.GO_TO_HOME);
                    // TODO : Replace this with equivalent fragment call
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

    private static void setProductAdditionalActionMenu(View row, final Product product,
                                                       final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                       final BaseActivity context,
                                                       final BaseFragment fragment) {
        final ImageView imgProductAdditionalAction = (ImageView) row.findViewById(R.id.imgProductAdditionalAction);
        setShoppingDeleteButton(row, product, productViewDisplayDataHolder, context, fragment);
        if (productViewDisplayDataHolder.isShowShoppingListBtn() && productViewDisplayDataHolder.isLoggedInMember()
                && !product.getProductStatus().equalsIgnoreCase("N") && fragment != null) {
            imgProductAdditionalAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    MenuInflater menuInflater = popupMenu.getMenuInflater();
                    menuInflater.inflate(R.menu.product_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menuAddToShoppingList:
                                    ((ShoppingListNamesAware) fragment).setSelectedProductId(product.getSku());
                                    new ShoppingListNamesTask(fragment,
                                            MobileApiUrl.getBaseAPIUrl() + "sl-get-lists/", false).execute();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        } else {
            imgProductAdditionalAction.setVisibility(View.GONE);
        }
    }

    private static void setShoppingDeleteButton(View row, final Product product,
                                                final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                final BaseActivity context,
                                                final BaseFragment baseFragment) {

        // for logged in user display add to list icon
        final ImageView imgShoppingListDel = (ImageView) row.findViewById(R.id.imgShoppingListDel);

        if (productViewDisplayDataHolder.showShopListDeleteBtn()) {
            imgShoppingListDel.setVisibility(View.VISIBLE);
            imgShoppingListDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                    // set title
                    alertDialogBuilder.setTitle("BigBasket");

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(
                                    "Are you sure you want to delete this product from the shopping list?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            if (DataUtil.isInternetAvailable(context)) {
                                                ArrayList<String> shoppingListNames = new ArrayList<>();
                                                shoppingListNames.add(productViewDisplayDataHolder.getShoppingListName().getSlug());
                                                ShoppingListDoOperationTask shoppingListDoOperationTask = new ShoppingListDoOperationTask(context,
                                                        MobileApiUrl.getBaseAPIUrl() + "sl-delete-item/",
                                                        shoppingListNames, ShoppingListOption.DELETE_ITEM);
                                                ((ShoppingListNamesAware) baseFragment).setSelectedProductId(product.getSku());
                                                shoppingListDoOperationTask.execute();
                                            } else {
                                                context.showToast("No internet connection found!");
                                            }
                                        }
                                    }
                            )
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                        }
                                    }
                            );


                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });
        } else {
            imgShoppingListDel.setVisibility(View.GONE);
        }
    }

    private static void setBasketAndAvailabilityViews(View row, final Product product,
                                                      final ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                                      final BaseActivity context,
                                                      final BaseFragment baseFragment) {
        final Button btnAddToBasket = (Button) row.findViewById(R.id.btnAddToBasket);
        final ImageView imgDecBasketQty = (ImageView) row.findViewById(R.id.imgDecBasketQty);
        final TextView txtInBasket = (TextView) row.findViewById(R.id.txtInBasket);
        final ImageView imgIncBasketQty = (ImageView) row.findViewById(R.id.imgIncBasketQty);

        final EditText editTextQty = (EditText) row.findViewById(R.id.editTextQty);
        final ImageView imgShoppingListAddToBasket = (ImageView) row.findViewById(R.id.imgProductAdditionalAction);

        TextView txtOutOfStockORNotForSale = (TextView) row.findViewById(R.id.txtOutOfStockORNotForSale);

        if (productViewDisplayDataHolder.isShowBasketBtn()) {
            if (product.getProductStatus().equalsIgnoreCase("A")) {
                int noOfItemsInCart = product.getNoOfItemsInCart();

                if (noOfItemsInCart > 0) {
                    txtInBasket.setText(noOfItemsInCart + " in basket");
                    txtInBasket.setVisibility(View.VISIBLE);
                    imgDecBasketQty.setVisibility(View.VISIBLE);
                    imgIncBasketQty.setVisibility(View.VISIBLE);

                    btnAddToBasket.setVisibility(View.GONE);
                    editTextQty.setVisibility(View.GONE);
                } else {
                    txtInBasket.setText("");
                    txtInBasket.setVisibility(View.GONE);
                    imgDecBasketQty.setVisibility(View.GONE);
                    imgIncBasketQty.setVisibility(View.GONE);

                    btnAddToBasket.setVisibility(View.VISIBLE);
                    editTextQty.setVisibility(View.VISIBLE);
                }

                imgIncBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (DataUtil.isInternetAvailable(context) && !TextUtils.isEmpty(editTextQty.getText())) {
                            BasketOperationTask basketOperationTask = new BasketOperationTask(baseFragment,
                                    MobileApiUrl.getBaseAPIUrl() + Constants.CART_INC, BasketOperation.ADD, product,
                                    txtInBasket, imgDecBasketQty, imgIncBasketQty, btnAddToBasket, editTextQty);
                            basketOperationTask.execute();

                        } else {
                            productViewDisplayDataHolder.getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
                        }
                    }
                });

                imgDecBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (DataUtil.isInternetAvailable(context) && !TextUtils.isEmpty(editTextQty.getText())) {
                            BasketOperationTask myTask = new BasketOperationTask(baseFragment,
                                    MobileApiUrl.getBaseAPIUrl() + Constants.CART_DEC, BasketOperation.DELETE,
                                    product, txtInBasket, imgDecBasketQty, imgIncBasketQty, btnAddToBasket, editTextQty);
                            myTask.execute();
                        } else {
                            productViewDisplayDataHolder.getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
                        }
                    }

                });

                btnAddToBasket.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (DataUtil.isInternetAvailable(context) && !TextUtils.isEmpty(editTextQty.getText())) {
                            if (editTextQty.getText() == null || TextUtils.isEmpty(editTextQty.getText().toString())) {
                                context.showToast("Please enter a valid quantity");
                                return;
                            }
                            String qty = editTextQty.getText() != null ? editTextQty.getText().toString() : "1";
                            BasketOperationTask basketOperationTask = new BasketOperationTask(baseFragment,
                                    MobileApiUrl.getBaseAPIUrl() + Constants.CART_INC, BasketOperation.ADD, product,
                                    txtInBasket, imgDecBasketQty, imgIncBasketQty, btnAddToBasket, editTextQty, qty);
                            basketOperationTask.execute();
                        } else {
                            productViewDisplayDataHolder.getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
                        }
                    }
                });
                if (productViewDisplayDataHolder.isLoggedInMember()) {
                    // Because for a out-of-stock, this button will be hidden, so during
                    // list view recycling, making it visible.
                    imgShoppingListAddToBasket.setVisibility(View.VISIBLE);
                }
                txtOutOfStockORNotForSale.setVisibility(View.GONE);
            } else {
                txtInBasket.setVisibility(View.GONE);
                imgDecBasketQty.setVisibility(View.GONE);
                imgIncBasketQty.setVisibility(View.GONE);
                editTextQty.setVisibility(View.GONE);

                btnAddToBasket.setVisibility(View.GONE);
                txtOutOfStockORNotForSale.setVisibility(View.VISIBLE);
                txtOutOfStockORNotForSale.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
                imgShoppingListAddToBasket.setVisibility(View.GONE);
                if (product.getProductStatus().equalsIgnoreCase("0") || product.getProductStatus().equalsIgnoreCase("O")) {  // zero not O
                    txtOutOfStockORNotForSale.setText("Out of Stock");
                } else {
                    txtOutOfStockORNotForSale.setText("Not for sale");
                }
            }
        } else {
            txtInBasket.setVisibility(View.GONE);
            imgDecBasketQty.setVisibility(View.GONE);
            imgIncBasketQty.setVisibility(View.GONE);
            btnAddToBasket.setVisibility(View.GONE);
            editTextQty.setVisibility(View.GONE);
        }
    }
}
