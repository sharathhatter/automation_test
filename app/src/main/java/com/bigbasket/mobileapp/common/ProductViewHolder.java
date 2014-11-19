package com.bigbasket.mobileapp.common;


import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;

public class ProductViewHolder {
    private ImageView imgProduct;
    private ImageView imgBby;
    private TextView txtIsNewProduct;
    private Spinner spinnerPackageDesc;
    private TextView packageDescTextView;
    private TextView txtProductBrand;
    private TextView txtProductDesc;
    private TextView labelMrp;
    private TextView txtSalePrice;
    private TextView txtMrp;
    private TextView txtSave;
    private ImageView valueStartForSaveTxt;
    private TextView txtPromoLabel;
    private TextView txtPromoDesc;
    private TextView txtPromoAddSavings;
    private ImageView imgProductAdditionalAction;
    private ImageView imgShoppingListDel;
    private Button btnAddToBasket;
    private ImageView imgDecBasketQty;
    private TextView txtInBasket;
    private ImageView imgIncBasketQty;
    private EditText editTextQty;
    private ImageView imgShoppingListAddToBasket;
    private TextView txtOutOfStockORNotForSale;
    private View row;

    public ProductViewHolder(View row) {
        this.row = row;
    }

    public ImageView getImgProduct() {
        if (imgProduct == null) {
            imgProduct = (ImageView) row.findViewById(R.id.imgProduct);
        }
        return imgProduct;
    }

    public ImageView getImgBby() {
        if (imgBby == null) {
            imgBby = (ImageView) row.findViewById(R.id.imgBBY);
        }
        return imgBby;
    }

    public TextView getTxtIsNewProduct() {
        if (txtIsNewProduct == null) {
            txtIsNewProduct = (TextView) row.findViewById(R.id.imgNew);
        }
        return txtIsNewProduct;
    }

    public Spinner getSpinnerPackageDesc() {
        if (spinnerPackageDesc == null) {
            spinnerPackageDesc = (Spinner) row.findViewById(R.id.spinnerPackageDesc);
        }
        return spinnerPackageDesc;
    }

    public TextView getPackageDescTextView() {
        if (packageDescTextView == null) {
            packageDescTextView = (TextView) row.findViewById(R.id.txtPackageDesc);
        }
        return packageDescTextView;
    }

    public TextView getTxtProductBrand() {
        if (txtProductBrand == null) {
            txtProductBrand = (TextView) row.findViewById(R.id.txtProductBrand);
        }
        return txtProductBrand;
    }

    public TextView getTxtProductDesc() {
        if (txtProductDesc == null) {
            txtProductDesc = (TextView) row.findViewById(R.id.txtProductDesc);
        }
        return txtProductDesc;
    }

    public TextView getLabelMrp() {
        if (labelMrp == null) {
            labelMrp = (TextView) row.findViewById(R.id.labelMrp);
        }
        return labelMrp;
    }

    public TextView getTxtSalePrice() {
        if (txtSalePrice == null) {
            txtSalePrice = (TextView) row.findViewById(R.id.txtSalePrice);
        }
        return txtSalePrice;
    }

    public TextView getTxtMrp() {
        if (txtMrp == null) {
            txtMrp = (TextView) row.findViewById(R.id.txtMrp);
        }
        return txtMrp;
    }

    public TextView getTxtSave() {
        if (txtSave == null) {
            txtSave = (TextView) row.findViewById(R.id.txtSave);
        }
        return txtSave;
    }

    public ImageView getValueStartForSaveTxt() {
        if (valueStartForSaveTxt == null) {
            valueStartForSaveTxt = (ImageView) row.findViewById(R.id.valueStarForSaveTxt);
        }
        return valueStartForSaveTxt;
    }

    public TextView getTxtPromoLabel() {
        if (txtPromoLabel == null) {
            txtPromoLabel = (TextView) row.findViewById(R.id.promoLabel);
        }
        return txtPromoLabel;
    }

    public TextView getTxtPromoDesc() {
        if (txtPromoDesc == null) {
            txtPromoDesc = (TextView) row.findViewById(R.id.txtPromoName);
        }
        return txtPromoDesc;
    }

    public TextView getTxtPromoAddSavings() {
        if (txtPromoAddSavings == null) {
            txtPromoAddSavings = (TextView) row.findViewById(R.id.txtPromoAddSavings);
        }
        return txtPromoAddSavings;
    }

    public ImageView getImgProductAdditionalAction() {
        if (imgProductAdditionalAction == null) {
            imgProductAdditionalAction = (ImageView) row.findViewById(R.id.imgProductAdditionalAction);
        }
        return imgProductAdditionalAction;
    }

    public ImageView getImgShoppingListDel() {
        if (imgShoppingListDel == null) {
            imgShoppingListDel = (ImageView) row.findViewById(R.id.imgShoppingListDel);
        }
        return imgShoppingListDel;
    }

    public Button getBtnAddToBasket() {
        if (btnAddToBasket == null) {
            btnAddToBasket = (Button) row.findViewById(R.id.btnAddToBasket);
        }
        return btnAddToBasket;
    }

    public ImageView getImgDecBasketQty() {
        if (imgDecBasketQty == null) {
            imgDecBasketQty = (ImageView) row.findViewById(R.id.imgDecBasketQty);
        }
        return imgDecBasketQty;
    }

    public TextView getTxtInBasket() {
        if (txtInBasket == null) {
            txtInBasket = (TextView) row.findViewById(R.id.txtInBasket);
        }
        return txtInBasket;
    }

    public ImageView getImgIncBasketQty() {
        if (imgIncBasketQty == null) {
            imgIncBasketQty = (ImageView) row.findViewById(R.id.imgIncBasketQty);
        }
        return imgIncBasketQty;
    }

    public EditText getEditTextQty() {
        if (editTextQty == null) {
            editTextQty = (EditText) row.findViewById(R.id.editTextQty);
        }
        return editTextQty;
    }

    public ImageView getImgShoppingListAddToBasket() {
        if (imgShoppingListAddToBasket == null) {
            imgShoppingListAddToBasket = (ImageView) row.findViewById(R.id.imgProductAdditionalAction);
        }
        return imgShoppingListAddToBasket;
    }

    public TextView getTxtOutOfStockORNotForSale() {
        if (txtOutOfStockORNotForSale == null) {
            txtOutOfStockORNotForSale = (TextView) row.findViewById(R.id.txtOutOfStockORNotForSale);
        }
        return txtOutOfStockORNotForSale;
    }
}
