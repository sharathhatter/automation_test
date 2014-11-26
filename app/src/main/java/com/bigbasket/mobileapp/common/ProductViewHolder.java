package com.bigbasket.mobileapp.common;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;

public class ProductViewHolder extends RecyclerView.ViewHolder {
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

    public ProductViewHolder(View itemView) {
        super(itemView);
    }

    public ImageView getImgProduct() {
        if (imgProduct == null) {
            imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
        }
        return imgProduct;
    }

    public ImageView getImgBby() {
        if (imgBby == null) {
            imgBby = (ImageView) itemView.findViewById(R.id.imgBBY);
        }
        return imgBby;
    }

    public TextView getTxtIsNewProduct() {
        if (txtIsNewProduct == null) {
            txtIsNewProduct = (TextView) itemView.findViewById(R.id.imgNew);
        }
        return txtIsNewProduct;
    }

    public Spinner getSpinnerPackageDesc() {
        if (spinnerPackageDesc == null) {
            spinnerPackageDesc = (Spinner) itemView.findViewById(R.id.spinnerPackageDesc);
        }
        return spinnerPackageDesc;
    }

    public TextView getPackageDescTextView() {
        if (packageDescTextView == null) {
            packageDescTextView = (TextView) itemView.findViewById(R.id.txtPackageDesc);
        }
        return packageDescTextView;
    }

    public TextView getTxtProductBrand() {
        if (txtProductBrand == null) {
            txtProductBrand = (TextView) itemView.findViewById(R.id.txtProductBrand);
        }
        return txtProductBrand;
    }

    public TextView getTxtProductDesc() {
        if (txtProductDesc == null) {
            txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
        }
        return txtProductDesc;
    }

    public TextView getLabelMrp() {
        if (labelMrp == null) {
            labelMrp = (TextView) itemView.findViewById(R.id.labelMrp);
        }
        return labelMrp;
    }

    public TextView getTxtSalePrice() {
        if (txtSalePrice == null) {
            txtSalePrice = (TextView) itemView.findViewById(R.id.txtSalePrice);
        }
        return txtSalePrice;
    }

    public TextView getTxtMrp() {
        if (txtMrp == null) {
            txtMrp = (TextView) itemView.findViewById(R.id.txtMrp);
        }
        return txtMrp;
    }

    public TextView getTxtSave() {
        if (txtSave == null) {
            txtSave = (TextView) itemView.findViewById(R.id.txtSave);
        }
        return txtSave;
    }

    public ImageView getValueStartForSaveTxt() {
        if (valueStartForSaveTxt == null) {
            valueStartForSaveTxt = (ImageView) itemView.findViewById(R.id.valueStarForSaveTxt);
        }
        return valueStartForSaveTxt;
    }

    public TextView getTxtPromoLabel() {
        if (txtPromoLabel == null) {
            txtPromoLabel = (TextView) itemView.findViewById(R.id.promoLabel);
        }
        return txtPromoLabel;
    }

    public TextView getTxtPromoDesc() {
        if (txtPromoDesc == null) {
            txtPromoDesc = (TextView) itemView.findViewById(R.id.txtPromoName);
        }
        return txtPromoDesc;
    }

    public TextView getTxtPromoAddSavings() {
        if (txtPromoAddSavings == null) {
            txtPromoAddSavings = (TextView) itemView.findViewById(R.id.txtPromoAddSavings);
        }
        return txtPromoAddSavings;
    }

    public ImageView getImgProductAdditionalAction() {
        if (imgProductAdditionalAction == null) {
            imgProductAdditionalAction = (ImageView) itemView.findViewById(R.id.imgProductAdditionalAction);
        }
        return imgProductAdditionalAction;
    }

    public ImageView getImgShoppingListDel() {
        if (imgShoppingListDel == null) {
            imgShoppingListDel = (ImageView) itemView.findViewById(R.id.imgShoppingListDel);
        }
        return imgShoppingListDel;
    }

    public Button getBtnAddToBasket() {
        if (btnAddToBasket == null) {
            btnAddToBasket = (Button) itemView.findViewById(R.id.btnAddToBasket);
        }
        return btnAddToBasket;
    }

    public ImageView getImgDecBasketQty() {
        if (imgDecBasketQty == null) {
            imgDecBasketQty = (ImageView) itemView.findViewById(R.id.imgDecBasketQty);
        }
        return imgDecBasketQty;
    }

    public TextView getTxtInBasket() {
        if (txtInBasket == null) {
            txtInBasket = (TextView) itemView.findViewById(R.id.txtInBasket);
        }
        return txtInBasket;
    }

    public ImageView getImgIncBasketQty() {
        if (imgIncBasketQty == null) {
            imgIncBasketQty = (ImageView) itemView.findViewById(R.id.imgIncBasketQty);
        }
        return imgIncBasketQty;
    }

    public EditText getEditTextQty() {
        if (editTextQty == null) {
            editTextQty = (EditText) itemView.findViewById(R.id.editTextQty);
        }
        return editTextQty;
    }

    public ImageView getImgShoppingListAddToBasket() {
        if (imgShoppingListAddToBasket == null) {
            imgShoppingListAddToBasket = (ImageView) itemView.findViewById(R.id.imgProductAdditionalAction);
        }
        return imgShoppingListAddToBasket;
    }

    public TextView getTxtOutOfStockORNotForSale() {
        if (txtOutOfStockORNotForSale == null) {
            txtOutOfStockORNotForSale = (TextView) itemView.findViewById(R.id.txtOutOfStockORNotForSale);
        }
        return txtOutOfStockORNotForSale;
    }
}
