package com.bigbasket.mobileapp.activity.order.uiv3;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.interfaces.OnUpdateReserveQtyAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.order.CheckoutProduct;
import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.task.CoUpdateReservationTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

public class CheckoutQCActivity extends BackButtonActivity implements OnUpdateReserveQtyAware {
    private COReserveQuantity coReserveQuantity;
    private String categoryName = "";
    private boolean mAlreadyRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doQc();
    }

    private void doQc() {
        //int qcLen = getIntent().getIntExtra(Constants.QC_LEN, -100);
        coReserveQuantity = getIntent().getParcelableExtra(Constants.CO_RESERVE_QTY_DATA);
        if (coReserveQuantity.getQc_len() == 0) {
            launchAddressSelection();
        } else {
            renderQcPage();
        }
    }

    private void launchAddressSelection() {
        Intent intent = new Intent(this, BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ADDRESS_SELECTION);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAlreadyRunning) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAlreadyRunning = true;
    }

    private void renderQcPage() {
        if (coReserveQuantity.isStatus()) {
            if (!coReserveQuantity.isQcHasErrors()) {
                showAlertDialog(null, getString(R.string.checkinternet), Constants.GO_TO_HOME_STRING);
            } else {
                createArrayListOfProducts();
            }
        }
    }

    public void onUpdateReserveSuccessResponse() {
        callQCApi();
    }

    private void callQCApi() {
        if (checkInternetConnection()) {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
            String pharmaPrescriptionId = prefer.getString(Constants.PHARMA_PRESCRIPTION_ID, null);
            new COReserveQuantityCheckTask<>(this, pharmaPrescriptionId).execute();
        } else {
            showAlertDialog(null, getString(R.string.checkinternet), Constants.GO_TO_HOME_STRING);
        }
    }

    @Override
    public COReserveQuantity getCOReserveQuantity() {
        return coReserveQuantity;
    }

    @Override
    public void setCOReserveQuantity(COReserveQuantity coReserveQuantity) {
        this.coReserveQuantity = coReserveQuantity;
    }

    @Override
    public void onCOReserveQuantityCheck() {
        renderQcORAddress();
    }

    private void renderQcORAddress() {
        if (coReserveQuantity.getQc_len() == 0) {
            launchAddressSelection();
        } else {
            renderQcPage();
        }
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, final Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.GO_TO_HOME_STRING:
                    goToHome();
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }


    private void createArrayListOfProducts() {
        if (coReserveQuantity.isQcHasErrors()) {
            ArrayList<CheckoutProduct> productWithNoStockList = new ArrayList<>();
            ArrayList<CheckoutProduct> productWithSomeStockList = new ArrayList<>();
            for (int i = 0; i < coReserveQuantity.getQCErrorData().size(); i++) {
                QCErrorData qcErrorData;
                qcErrorData = coReserveQuantity.getQCErrorData().get(i);
                Product product = qcErrorData.getProduct();
                CheckoutProduct item_details = new CheckoutProduct();
                item_details.setReserveQuantity(String.valueOf(qcErrorData.getReservedQuantity()));
                item_details.setOriginalQuantity(String.valueOf(qcErrorData.getOriginalQuantity()));
                item_details.setDescription(product.getDescription());
                item_details.setSpprice(product.getSellPrice());
                item_details.setMrp(product.getMrp());
                item_details.setBrand(product.getBrand());
                item_details.setDiscountValue(product.getDiscountValue());
                item_details.setId(product.getSku());
                item_details.setWeight(product.getWeight());
                item_details.setCategoryName(product.getTopLevelCategoryName());
                item_details.setTopLevelCategorySlug(product.getTopLevelCategorySlug());
                if (!qcErrorData.getReservedQuantity().equals("0")) {
                    productWithSomeStockList.add(item_details);
                } else {
                    productWithNoStockList.add(item_details);
                }
            }
            renderCheckOut(productWithNoStockList, productWithSomeStockList);
            trackEvent(TrackingAware.CHECKOUT_QC_SHOWN, null);
        } else {
            showAlertDialogFinish(null, getString(R.string.INTERNAL_SERVER_ERROR));
        }

    }

    /*
    private boolean isViaInvoice() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean viaInvoice = sharedPreferences.getBoolean(Constants.VIA_INVOICE, false);
        removeViaInvoiceFlag();
        return viaInvoice;
    }
    */

    private void renderCheckOut(final ArrayList<CheckoutProduct> productWithNoStockList,
                                final ArrayList<CheckoutProduct> productWithSomeStockList) {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        base.addView(contentView);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int productWithNoStockListSize = productWithNoStockList.size();
        int productWithSomeStockListSize = productWithSomeStockList.size();
        if (productWithNoStockListSize > 0 || productWithSomeStockListSize > 0) {

            contentView.removeAllViews();
            contentView.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));
            contentView.setPadding(0, 0, 0, 10);

            RelativeLayout layoutRelativeMain = (RelativeLayout) inflater.inflate(R.layout.uiv3_checkout_qc_scroll, null);
            LinearLayout linearLayoutViewQC = (LinearLayout) layoutRelativeMain.findViewById(R.id.layoutMainCheckoutQc);
            //FloatingActionButton btnFabProceedQc = (FloatingActionButton) layoutRelativeMain.findViewById(R.id.btnFabProceedQc);
            contentView.addView(layoutRelativeMain);

            if (productWithNoStockListSize > 0) {
                showQcMsg(linearLayoutViewQC, false);
                for (int i = 0; i < productWithNoStockListSize; i++) {
                    // show product's top category
                    if (!categoryName.equalsIgnoreCase(productWithNoStockList.get(i).getCategoryName()) &&
                            !TextUtils.isEmpty(productWithNoStockList.get(i).getCategoryName())) {
                        View categoryRow = inflater.inflate(R.layout.uiv3_category_row, null);
                        TextView txtTopCategory = (TextView) categoryRow.findViewById(R.id.txtTopCategory);
                        categoryName = productWithNoStockList.get(i).getCategoryName();
                        txtTopCategory.setText(productWithNoStockList.get(i).getCategoryName());
                        linearLayoutViewQC.addView(categoryRow);
                    }

                    // no stock product
                    RelativeLayout layoutWithNoStockProducts = (RelativeLayout) inflater.inflate(R.layout.uiv3_out_of_stock_product_layout, null);

//                    TextView txtIndex = (TextView) layoutWithNoStockProducts.findViewById(R.id.txtIndex);
//                    txtIndex.setText(String.valueOf(i+1));

                    TextView txtProductBrand = (TextView) layoutWithNoStockProducts.findViewById(R.id.txtProductBrand);
                    txtProductBrand.setText(productWithNoStockList.get(i).getBrand());

                    TextView txtProductDesc = (TextView) layoutWithNoStockProducts.findViewById(R.id.txtProductDesc);
                    txtProductDesc.setText(productWithNoStockList.get(i).getDescription());

                    TextView txtWeight = (TextView) layoutWithNoStockProducts.findViewById(R.id.txtWeight);
                    txtWeight.setText(productWithNoStockList.get(i).getWeight());

                    RelativeLayout layoutSomeStock = (RelativeLayout) layoutWithNoStockProducts.findViewById(R.id.layoutSomeStock);
                    layoutSomeStock.setVisibility(View.GONE);

                    //ImageView imgRemove = (ImageView) layoutWithNoStockProducts.findViewById(R.id.imgRemove);
                    //imgRemove.setVisibility(View.GONE);

                    //Button btnSimilarProducts = (Button) layoutWithNoStockProducts.findViewById(R.id.btnSimilarProducts);
                    //btnSimilarProducts.setVisibility(View.GONE);

                    linearLayoutViewQC.addView(layoutWithNoStockProducts);
                }

            }

            if (productWithSomeStockListSize > 0) {
                showQcMsg(linearLayoutViewQC, true);
                categoryName = "";
                for (int i = 0; i < productWithSomeStockListSize; i++) {
                    // show product's top category
                    if (!categoryName.equalsIgnoreCase(productWithSomeStockList.get(i).getCategoryName()) &&
                            !TextUtils.isEmpty(productWithSomeStockList.get(i).getCategoryName())) {
                        View categoryRow = inflater.inflate(R.layout.uiv3_category_row, null);
                        TextView txtTopCategory = (TextView) categoryRow.findViewById(R.id.txtTopCategory);
                        categoryName = productWithSomeStockList.get(i).getCategoryName();
                        txtTopCategory.setText(productWithSomeStockList.get(i).getCategoryName());
                        linearLayoutViewQC.addView(categoryRow);
                    }

                    // some stock product
                    RelativeLayout layoutWithSomeStockProducts = (RelativeLayout) inflater.inflate(R.layout.uiv3_out_of_stock_product_layout, null);

                    TextView txtOutOfStock = (TextView) layoutWithSomeStockProducts.findViewById(R.id.txtOutOfStock);
                    txtOutOfStock.setVisibility(View.GONE);

                    TextView txtProductBrand = (TextView) layoutWithSomeStockProducts.findViewById(R.id.txtProductBrand);
                    txtProductBrand.setText(productWithSomeStockList.get(i).getBrand());

                    TextView txtProductDesc = (TextView) layoutWithSomeStockProducts.findViewById(R.id.txtProductDesc);
                    txtProductDesc.setText(productWithSomeStockList.get(i).getDescription());

                    TextView txtWeight = (TextView) layoutWithSomeStockProducts.findViewById(R.id.txtWeight);
                    txtWeight.setVisibility(View.GONE);

                    RelativeLayout layoutSomeStock = (RelativeLayout) layoutWithSomeStockProducts.findViewById(R.id.layoutSomeStock);
                    layoutSomeStock.setVisibility(View.VISIBLE);

                    TextView txtSelected = (TextView) layoutWithSomeStockProducts.findViewById(R.id.txtSelected);
                    String prefixSelect = getString(R.string.check_out_selected);
                    String postfixSelect = productWithSomeStockList.get(i).getOriginalQuantity();
                    SpannableString spannableTxtSelected = new SpannableString(prefixSelect + " " + postfixSelect);
                    spannableTxtSelected.setSpan(new ForegroundColorSpan(0xff709d03), 0, prefixSelect.length(), 0);
                    txtSelected.setText(spannableTxtSelected);

                    TextView txtAvailable = (TextView) layoutWithSomeStockProducts.findViewById(R.id.txtAvailable);
                    String prefixAvailable = getString(R.string.check_out_available);
                    String postfixAvailable = productWithSomeStockList.get(i).getReserveQuantity();
                    SpannableString spannableTxtAvailable = new SpannableString(prefixAvailable + " " + postfixAvailable);
                    spannableTxtAvailable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uiv3_action_bar_background)),
                            0, prefixSelect.length(), 0);
                    txtAvailable.setText(spannableTxtAvailable);

                    TextView txtSalePrice = (TextView) layoutWithSomeStockProducts.findViewById(R.id.txtSalePrice);
                    if (!TextUtils.isEmpty(productWithSomeStockList.get(i).getSpprice())) {
                        String preFixSalePrice = "Sale Price: `";
                        String postFixSalePrice = getDecimalAmount((Double.parseDouble(productWithSomeStockList.get(i).getSpprice())));
                        int prefixBalLen = preFixSalePrice.length();
                        SpannableString spannableSalePrice = new SpannableString(preFixSalePrice + " " + postFixSalePrice);
                        spannableSalePrice.setSpan(new CustomTypefaceSpan("", faceRupee), prefixBalLen - 1,
                                prefixBalLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        txtSalePrice.setText(spannableSalePrice);
                    } else {
                        txtSalePrice.setVisibility(View.GONE);
                    }

                    TextView txtSaving = (TextView) layoutWithSomeStockProducts.findViewById(R.id.txtSaving);
                    if (!TextUtils.isEmpty(productWithSomeStockList.get(i).getDiscountValue())) {
                        String preFixSaving = "Saving: `";
                        String postFixSaving = getDecimalAmount(Double.parseDouble(productWithSomeStockList.get(i).getDiscountValue()));
                        int prefixBalLen = preFixSaving.length();
                        SpannableString spannableSaving = new SpannableString(preFixSaving + " " + postFixSaving);
                        spannableSaving.setSpan(new CustomTypefaceSpan("", faceRupee), prefixBalLen - 1,
                                prefixBalLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        txtSaving.setText(spannableSaving);
                    } else {
                        txtSaving.setVisibility(View.GONE);
                    }

                    final ImageView imgProductCheckOutQCAdditionalAction = (ImageView) layoutWithSomeStockProducts.
                            findViewById(R.id.imgProductCheckOutQCAdditionalAction);
                    imgProductCheckOutQCAdditionalAction.setVisibility(View.VISIBLE);
                    imgProductCheckOutQCAdditionalAction.setTag(productWithSomeStockList.get(i).getId());
                    imgProductCheckOutQCAdditionalAction.setId(i);
                    imgProductCheckOutQCAdditionalAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popupMenu = new PopupMenu(getCurrentActivity(), v);
                            MenuInflater menuInflater = popupMenu.getMenuInflater();
                            menuInflater.inflate(R.menu.check_out_qc, popupMenu.getMenu());
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.menuAddSimilarQCProduct:
                                            Intent data = new Intent(getCurrentActivity(), BBActivity.class);
                                            data.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SUBCAT_FRAGMENT);
                                            data.putExtra(Constants.TOP_CATEGORY_SLUG,
                                                    productWithSomeStockList.get(imgProductCheckOutQCAdditionalAction.getId()).getTopLevelCategorySlug());
                                            data.putExtra(Constants.TOP_CATEGORY_NAME,
                                                    productWithSomeStockList.get(imgProductCheckOutQCAdditionalAction.getId()).getCategoryName());
                                            startActivityForResult(data, NavigationCodes.GO_TO_HOME);
                                            return true;
                                        case R.id.menuDeleteQCProduct:
                                            new CoUpdateReservationTask<>(getCurrentActivity(),
                                                    true, String.valueOf(imgProductCheckOutQCAdditionalAction.getTag()),
                                                    0).execute();
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });
                            popupMenu.show();
                        }
                    });

                    linearLayoutViewQC.addView(layoutWithSomeStockProducts);
                }
            }

            Button btnContinue = UIUtil.getPrimaryButton(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            int btnMargin = (int) getResources().getDimension(R.dimen.margin_large);
            layoutParams.setMargins(btnMargin, btnMargin, btnMargin, btnMargin);
            btnContinue.setLayoutParams(layoutParams);
            btnContinue.setTypeface(faceRobotoRegular);
            btnContinue.setText("Continue");
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CoUpdateReservationTask<>(getCurrentActivity(), false, productWithNoStockList, productWithSomeStockList).execute();
                }
            });
            contentView.addView(btnContinue);
        } else {
            showAlertDialogFinish(null, getString(R.string.INTERNAL_SERVER_ERROR));
        }

    }

    private void showQcMsg(LinearLayout linearLayoutViewQC, boolean forNoProductView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.uiv3_checkout_msg, null);
        TextView txtOutOfStockMsg1 = (TextView) view.findViewById(R.id.txtOutOfStockMsg1);
        txtOutOfStockMsg1.setTypeface(faceRobotoRegular);
        TextView txtOutOfStockMsg2 = (TextView) view.findViewById(R.id.txtOutOfStockMsg2);
        txtOutOfStockMsg2.setTypeface(faceRobotoRegular);
        if (forNoProductView) {
            txtOutOfStockMsg1.setText(getString(R.string.out_of_stock_msg3));
            txtOutOfStockMsg2.setVisibility(View.VISIBLE);
        }
        linearLayoutViewQC.addView(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NavigationCodes.GO_TO_QC) {
            mAlreadyRunning = false;
            doQc();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
