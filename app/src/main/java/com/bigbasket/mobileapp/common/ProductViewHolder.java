package com.bigbasket.mobileapp.common;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    private ImageView imgProduct;
    private ImageView imgBby;
    private TextView txtIsNewProduct;
    private Button btnMorePackSizes;
    private TextView packageDescTextView;
    private TextView txtProductDesc;
    private TextView txtSalePrice;
    private TextView txtMrp;
    private TextView txtSave;
    private TextView txtPromoLabel;
    private TextView txtPromoDesc;
    private TextView txtPromoAddSavings;
    private Button btnAddToBasket;
    private TextView txtDecBasketQty;
    private TextView txtInBasket;
    private TextView txtIncBasketQty;
    private EditText editTextQty;
    private TextView txtOutOfStockORNotForSale;
    private ImageView imgProductOverflowAction;

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

    public Button getBtnMorePackSizes() {
        if (btnMorePackSizes == null) {
            btnMorePackSizes = (Button) itemView.findViewById(R.id.btnMorePackSizes);
        }
        return btnMorePackSizes;
    }

    public TextView getPackageDescTextView() {
        if (packageDescTextView == null) {
            packageDescTextView = (TextView) itemView.findViewById(R.id.txtPackageDesc);
        }
        return packageDescTextView;
    }

    public TextView getTxtProductDesc() {
        if (txtProductDesc == null) {
            txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
        }
        return txtProductDesc;
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

    public Button getBtnAddToBasket() {
        if (btnAddToBasket == null) {
            btnAddToBasket = (Button) itemView.findViewById(R.id.btnAddToBasket);
        }
        return btnAddToBasket;
    }

    public TextView getTxtDecBasketQty() {
        if (txtDecBasketQty == null) {
            txtDecBasketQty = (TextView) itemView.findViewById(R.id.txtDecBasketQty);
        }
        return txtDecBasketQty;
    }

    public TextView getTxtInBasket() {
        if (txtInBasket == null) {
            txtInBasket = (TextView) itemView.findViewById(R.id.txtInBasket);
        }
        return txtInBasket;
    }

    public TextView getTxtIncBasketQty() {
        if (txtIncBasketQty == null) {
            txtIncBasketQty = (TextView) itemView.findViewById(R.id.txtIncBasketQty);
        }
        return txtIncBasketQty;
    }

    public EditText getEditTextQty() {
        if (editTextQty == null) {
            editTextQty = (EditText) itemView.findViewById(R.id.editTextQty);
        }
        return editTextQty;
    }

    public TextView getTxtOutOfStockORNotForSale() {
        if (txtOutOfStockORNotForSale == null) {
            txtOutOfStockORNotForSale = (TextView) itemView.findViewById(R.id.txtOutOfStockORNotForSale);
        }
        return txtOutOfStockORNotForSale;
    }

    public ImageView getImgProductOverflowAction() {
        if (imgProductOverflowAction == null) {
            imgProductOverflowAction = (ImageView) itemView.findViewById(R.id.imgProductOverflowAction);
        }
        return imgProductOverflowAction;
    }
}
